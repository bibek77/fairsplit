import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { groupService, expenseService } from '../services';
import './AddExpensePage.css';

const AddExpensePage = () => {
    const { groupId } = useParams();
    const navigate = useNavigate();
    const [group, setGroup] = useState(null);
    const [description, setDescription] = useState('');
    const [amount, setAmount] = useState('');
    const [paidBy, setPaidBy] = useState('');
    const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
    const [useCustomSplit, setUseCustomSplit] = useState(false);
    const [contributions, setContributions] = useState({});
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        fetchGroup();
    }, [groupId]);

    const fetchGroup = async () => {
        try {
            const data = await groupService.getById(groupId);
            setGroup(data);
            setPaidBy(data.participants[0] || '');

            // Initialize contributions
            const initialContributions = {};
            data.participants.forEach(p => {
                initialContributions[p] = '';
            });
            setContributions(initialContributions);
        } catch (err) {
            console.error('Error fetching group:', err);
            navigate('/');
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!description.trim()) {
            newErrors.description = 'Description is required';
        }

        const amountNum = parseFloat(amount);
        if (!amount || isNaN(amountNum) || amountNum <= 0) {
            newErrors.amount = 'Amount must be a positive number';
        }

        if (!paidBy) {
            newErrors.paidBy = 'Please select who paid';
        }

        const selectedDate = new Date(date);
        const today = new Date();
        today.setHours(23, 59, 59, 999);
        if (selectedDate > today) {
            newErrors.date = 'Date cannot be in the future';
        }

        if (useCustomSplit) {
            const contributionValues = Object.values(contributions).map(v => parseFloat(v) || 0);
            const total = contributionValues.reduce((sum, val) => sum + val, 0);

            if (Math.abs(total - amountNum) > 0.01) {
                newErrors.contributions = `Contributions must sum to ${amountNum.toFixed(2)}`;
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const calculateSplitPreview = () => {
        if (!amount || !group) return null;

        const amountNum = parseFloat(amount);
        if (isNaN(amountNum)) return null;

        if (useCustomSplit) {
            return contributions;
        } else {
            const perPerson = amountNum / group.participants.length;
            const preview = {};
            group.participants.forEach(p => {
                preview[p] = perPerson.toFixed(2);
            });
            return preview;
        }
    };

    const handleContributionChange = (participant, value) => {
        setContributions({
            ...contributions,
            [participant]: value,
        });
        if (errors.contributions) {
            setErrors({ ...errors, contributions: null });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            setLoading(true);

            const expenseData = {
                description: description.trim(),
                amount: parseFloat(amount),
                paidBy,
                date,
            };

            if (useCustomSplit) {
                const customContributions = {};
                Object.entries(contributions).forEach(([participant, value]) => {
                    customContributions[participant] = parseFloat(value) || 0;
                });
                expenseData.contributions = customContributions;
            }

            await expenseService.add(groupId, expenseData);
            navigate(`/groups/${groupId}`);
        } catch (err) {
            const errorMessage = err.response?.data?.message || 'Failed to add expense. Please try again.';
            setErrors({ submit: errorMessage });
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = () => {
        navigate(`/groups/${groupId}`);
    };

    if (!group) {
        return <div className="add-expense-page"><div className="loading">Loading...</div></div>;
    }

    const splitPreview = calculateSplitPreview();

    return (
        <div className="add-expense-page">
            <div className="add-expense-container">
                <h1>Add Expense to {group.groupName}</h1>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="description">Description *</label>
                        <input
                            type="text"
                            id="description"
                            value={description}
                            onChange={(e) => {
                                setDescription(e.target.value);
                                if (errors.description) setErrors({ ...errors, description: null });
                            }}
                            placeholder="e.g., Dinner at restaurant"
                            className={errors.description ? 'error' : ''}
                        />
                        {errors.description && <span className="error-message">{errors.description}</span>}
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="amount">Amount * ($)</label>
                            <input
                                type="number"
                                id="amount"
                                value={amount}
                                onChange={(e) => {
                                    setAmount(e.target.value);
                                    if (errors.amount) setErrors({ ...errors, amount: null });
                                }}
                                placeholder="0.00"
                                step="0.01"
                                min="0"
                                className={errors.amount ? 'error' : ''}
                            />
                            {errors.amount && <span className="error-message">{errors.amount}</span>}
                        </div>

                        <div className="form-group">
                            <label htmlFor="date">Date *</label>
                            <input
                                type="date"
                                id="date"
                                value={date}
                                onChange={(e) => {
                                    setDate(e.target.value);
                                    if (errors.date) setErrors({ ...errors, date: null });
                                }}
                                max={new Date().toISOString().split('T')[0]}
                                className={errors.date ? 'error' : ''}
                            />
                            {errors.date && <span className="error-message">{errors.date}</span>}
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="paidBy">Paid By *</label>
                        <select
                            id="paidBy"
                            value={paidBy}
                            onChange={(e) => {
                                setPaidBy(e.target.value);
                                if (errors.paidBy) setErrors({ ...errors, paidBy: null });
                            }}
                            className={errors.paidBy ? 'error' : ''}
                        >
                            {group.participants.map(participant => (
                                <option key={participant} value={participant}>
                                    {participant}
                                </option>
                            ))}
                        </select>
                        {errors.paidBy && <span className="error-message">{errors.paidBy}</span>}
                    </div>

                    <div className="form-group">
                        <label className="checkbox-label">
                            <input
                                type="checkbox"
                                checked={useCustomSplit}
                                onChange={(e) => setUseCustomSplit(e.target.checked)}
                            />
                            Use custom split
                        </label>
                    </div>

                    {useCustomSplit && (
                        <div className="form-group">
                            <label>Custom Contributions</label>
                            {group.participants.map(participant => (
                                <div key={participant} className="contribution-input">
                                    <span className="participant-label">{participant}</span>
                                    <input
                                        type="number"
                                        value={contributions[participant]}
                                        onChange={(e) => handleContributionChange(participant, e.target.value)}
                                        placeholder="0.00"
                                        step="0.01"
                                        min="0"
                                    />
                                </div>
                            ))}
                            {errors.contributions && <span className="error-message">{errors.contributions}</span>}
                        </div>
                    )}

                    {splitPreview && (
                        <div className="split-preview">
                            <h4>Split Preview:</h4>
                            <div className="preview-items">
                                {Object.entries(splitPreview).map(([participant, share]) => (
                                    <div key={participant} className="preview-item">
                                        <span>{participant}</span>
                                        <span>${typeof share === 'number' ? share.toFixed(2) : share}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {errors.submit && (
                        <div className="submit-error">
                            {errors.submit}
                        </div>
                    )}

                    <div className="form-actions">
                        <button
                            type="button"
                            onClick={handleCancel}
                            className="cancel-button"
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="submit-button"
                            disabled={loading}
                        >
                            {loading ? 'Adding...' : 'Add Expense'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddExpensePage;
