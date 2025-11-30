import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { groupService } from '../services';
import './CreateGroupPage.css';

const CreateGroupPage = () => {
    const [groupName, setGroupName] = useState('');
    const [participants, setParticipants] = useState(['']);
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const validateForm = () => {
        const newErrors = {};

        // Validate group name
        if (!groupName.trim()) {
            newErrors.groupName = 'Group name is required';
        }

        // Validate participants
        const validParticipants = participants.filter(p => p.trim());
        if (validParticipants.length === 0) {
            newErrors.participants = 'At least one participant is required';
        } else if (validParticipants.length > 10) {
            newErrors.participants = 'Maximum 10 participants allowed';
        } else {
            // Check for duplicates (case-sensitive)
            const uniqueParticipants = new Set(validParticipants);
            if (uniqueParticipants.size !== validParticipants.length) {
                newErrors.participants = 'Participant names must be unique';
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleAddParticipant = () => {
        if (participants.length < 10) {
            setParticipants([...participants, '']);
        }
    };

    const handleRemoveParticipant = (index) => {
        if (participants.length > 1) {
            const newParticipants = participants.filter((_, i) => i !== index);
            setParticipants(newParticipants);
        }
    };

    const handleParticipantChange = (index, value) => {
        const newParticipants = [...participants];
        newParticipants[index] = value;
        setParticipants(newParticipants);
        // Clear errors on change
        if (errors.participants) {
            setErrors({ ...errors, participants: null });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            setLoading(true);
            const validParticipants = participants.filter(p => p.trim());

            await groupService.create({
                groupName: groupName.trim(),
                participants: validParticipants,
            });

            // Success - navigate to dashboard
            navigate('/');
        } catch (err) {
            const errorMessage = err.response?.data?.message || 'Failed to create group. Please try again.';
            setErrors({ submit: errorMessage });
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = () => {
        navigate('/');
    };

    return (
        <div className="create-group-page">
            <div className="create-group-container">
                <h1>Create New Group</h1>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="groupName">Group Name *</label>
                        <input
                            type="text"
                            id="groupName"
                            value={groupName}
                            onChange={(e) => {
                                setGroupName(e.target.value);
                                if (errors.groupName) {
                                    setErrors({ ...errors, groupName: null });
                                }
                            }}
                            placeholder="e.g., Weekend Trip"
                            className={errors.groupName ? 'error' : ''}
                        />
                        {errors.groupName && <span className="error-message">{errors.groupName}</span>}
                    </div>

                    <div className="form-group">
                        <label>Participants * (Max 10)</label>
                        {participants.map((participant, index) => (
                            <div key={index} className="participant-input">
                                <input
                                    type="text"
                                    value={participant}
                                    onChange={(e) => handleParticipantChange(index, e.target.value)}
                                    placeholder={`Participant ${index + 1}`}
                                    className={errors.participants ? 'error' : ''}
                                />
                                {participants.length > 1 && (
                                    <button
                                        type="button"
                                        onClick={() => handleRemoveParticipant(index)}
                                        className="remove-button"
                                    >
                                        ✕
                                    </button>
                                )}
                            </div>
                        ))}
                        {errors.participants && <span className="error-message">{errors.participants}</span>}

                        {participants.length < 10 && (
                            <button
                                type="button"
                                onClick={handleAddParticipant}
                                className="add-participant-button"
                            >
                                + Add Participant
                            </button>
                        )}
                    </div>

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
                            {loading ? 'Creating...' : 'Create Group'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CreateGroupPage;
