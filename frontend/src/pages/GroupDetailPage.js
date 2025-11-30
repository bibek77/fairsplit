import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { groupService, expenseService, settlementService } from '../services';
import ExpenseList from '../components/ExpenseList';
import SettlementSummary from '../components/SettlementSummary';
import MemberBalanceCard from '../components/MemberBalanceCard';
import './GroupDetailPage.css';

const GroupDetailPage = () => {
    const { groupId } = useParams();
    const navigate = useNavigate();
    const [group, setGroup] = useState(null);
    const [expenses, setExpenses] = useState([]);
    const [settlements, setSettlements] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchGroupData();
    }, [groupId]);

    const fetchGroupData = async () => {
        try {
            setLoading(true);
            setError(null);

            const [groupData, expensesData, settlementsData] = await Promise.all([
                groupService.getById(groupId),
                expenseService.getByGroup(groupId),
                settlementService.getSettlements(groupId),
            ]);

            setGroup(groupData);
            setExpenses(expensesData);
            setSettlements(settlementsData);
        } catch (err) {
            setError('Failed to load group details. Please try again.');
            console.error('Error fetching group data:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddExpense = () => {
        navigate(`/groups/${groupId}/expenses/new`);
    };

    const handleBack = () => {
        navigate('/');
    };

    if (loading) {
        return (
            <div className="group-detail-page">
                <div className="loading">Loading group details...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="group-detail-page">
                <div className="error">
                    {error}
                    <button onClick={fetchGroupData} className="retry-button">
                        Retry
                    </button>
                </div>
            </div>
        );
    }

    if (!group) {
        return (
            <div className="group-detail-page">
                <div className="error">Group not found</div>
            </div>
        );
    }

    return (
        <div className="group-detail-page">
            <div className="group-header">
                <button onClick={handleBack} className="back-button">
                    ← Back
                </button>
                <div className="group-info-header">
                    <h1>{group.groupName}</h1>
                    <div className="group-meta">
                        <span>👥 {group.participants.join(', ')}</span>
                        <span>💰 Total: ${group.totalExpense?.toFixed(2) || '0.00'}</span>
                    </div>
                </div>
                <button onClick={handleAddExpense} className="add-expense-button">
                    + Add Expense
                </button>
            </div>

            <div className="group-content">
                <div className="left-column">
                    <ExpenseList expenses={expenses} />
                </div>

                <div className="right-column">
                    <SettlementSummary settlements={settlements?.settlements || []} />
                    <MemberBalanceCard memberBalances={settlements?.memberBalances || {}} />
                </div>
            </div>
        </div>
    );
};

export default GroupDetailPage;
