import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import GroupCard from '../components/GroupCard';
import { groupService } from '../services';
import './Dashboard.css';

const Dashboard = () => {
    const [groups, setGroups] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        fetchGroups();
    }, []);

    const fetchGroups = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await groupService.getAll();
            setGroups(data);
        } catch (err) {
            setError('Failed to load groups. Please try again.');
            console.error('Error fetching groups:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateGroup = () => {
        navigate('/groups/new');
    };

    if (loading) {
        return (
            <div className="dashboard">
                <div className="loading">Loading groups...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="dashboard">
                <div className="error">
                    {error}
                    <button onClick={fetchGroups} className="retry-button">
                        Retry
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="dashboard">
            <div className="dashboard-header">
                <h1>FairSplit</h1>
                <button onClick={handleCreateGroup} className="create-button">
                    + Create New Group
                </button>
            </div>

            {groups.length === 0 ? (
                <div className="empty-state">
                    <p>No groups yet. Create your first group to get started!</p>
                </div>
            ) : (
                <div className="groups-grid">
                    {groups.map((group) => (
                        <GroupCard key={group.groupId} group={group} />
                    ))}
                </div>
            )}
        </div>
    );
};

export default Dashboard;
