import React from 'react';
import './SettlementSummary.css';

const SettlementSummary = ({ settlements }) => {
    if (!settlements || settlements.length === 0) {
        return (
            <div className="settlement-summary">
                <h3>Settlements</h3>
                <p className="empty-message">All settled up! 🎉</p>
            </div>
        );
    }

    return (
        <div className="settlement-summary">
            <h3>Who Owes Whom</h3>
            <div className="settlements">
                {settlements.map((settlement, index) => (
                    <div key={index} className="settlement-item">
                        <span className="from">{settlement.from}</span>
                        <span className="arrow">→</span>
                        <span className="to">{settlement.to}</span>
                        <span className="amount">${settlement.amount.toFixed(2)}</span>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default SettlementSummary;
