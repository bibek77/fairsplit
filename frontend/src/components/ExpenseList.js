import React from 'react';
import './ExpenseList.css';

const ExpenseList = ({ expenses }) => {
    if (!expenses || expenses.length === 0) {
        return (
            <div className="expense-list">
                <h3>Expenses</h3>
                <p className="empty-message">No expenses yet. Add your first expense!</p>
            </div>
        );
    }

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    };

    return (
        <div className="expense-list">
            <h3>Expenses</h3>
            <div className="expenses">
                {expenses.map((expense) => (
                    <div key={expense.expenseId} className="expense-item">
                        <div className="expense-header">
                            <span className="expense-date">{formatDate(expense.date)}</span>
                            <span className="expense-amount">${expense.amount.toFixed(2)}</span>
                        </div>
                        <div className="expense-details">
                            <p className="expense-description">{expense.description}</p>
                            <p className="expense-payer">Paid by: <strong>{expense.paidBy}</strong></p>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ExpenseList;
