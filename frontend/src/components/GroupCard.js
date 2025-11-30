import React from 'react';
import { useNavigate } from 'react-router-dom';
import './GroupCard.css';

const GroupCard = ({ group }) => {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate(`/groups/${group.groupId}`);
    };

    return (
        <div className="group-card" onClick={handleClick}>
            <h3>{group.groupName}</h3>
            <div className="group-info">
                <p className="participant-count">
                    👥 {group.participantCount} {group.participantCount === 1 ? 'participant' : 'participants'}
                </p>
                <p className="total-expense">
                    💰 ${group.totalExpense?.toFixed(2) || '0.00'}
                </p>
            </div>
        </div>
    );
};

export default GroupCard;
