import React from 'react';
import './MemberBalanceCard.css';

const MemberBalanceCard = ({ memberBalances }) => {
    if (!memberBalances || Object.keys(memberBalances).length === 0) {
        return null;
    }

    const getBalanceClass = (balance) => {
        if (balance > 0) return 'positive';
        if (balance < 0) return 'negative';
        return 'zero';
    };

    return (
        <div className="member-balance-card">
            <h3>Member Balances</h3>
            <div className="balances">
                {Object.entries(memberBalances).map(([name, balance]) => (
                    <div key={name} className="balance-item">
                        <div className="member-name">{name}</div>
                        <div className="balance-details">
                            <div className="balance-row">
                                <span className="label">Paid:</span>
                                <span className="value">${balance.totalPaid.toFixed(2)}</span>
                            </div>
                            <div className="balance-row">
                                <span className="label">Owed:</span>
                                <span className="value">${balance.totalOwed.toFixed(2)}</span>
                            </div>
                            <div className="balance-row net">
                                <span className="label">Net:</span>
                                <span className={`value ${getBalanceClass(balance.netBalance)}`}>
                                    ${balance.netBalance.toFixed(2)}
                                </span>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default MemberBalanceCard;
