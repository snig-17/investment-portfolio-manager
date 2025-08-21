import React, { useState } from "react";
import ApiService from "../services/ApiService";

function CashManager({ portfolioId, onCashUpdated }) {
    
    const [amount, setAmount] = useState("");
    const [reason, setReason] = useState("");
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");

    const handleDeposit = async () => {
        if (!amount || amount <= 0) {
            setMessage("Please enter a valid amount to deposit.");
            return;
        }

        try {
            setLoading(true);
            setMessage("");

            // call spring boot api to update cash balance
            await apiService.updateCashBalance(
                portfolioId, 
                parseFloat(amount), 
                reason || "Cash Deposit"
            );

            setMessage("Cash deposited successfully.");
            setAmount("");
            setReason("");

            if (onCashUpdated) {
                onCashUpdated(); // Notify parent component
            }
        } catch (error) {
            console.error("Error depositing cash:", error);
            setMessage("Failed to deposit cash. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    const handleWithdraw = async () => {
        if (!amount || amount <= 0) {
            setMessage("Please enter a valid amount to withdraw.");
            return;
        }

        try {
            setLoading(true);
            setMessage("");

            // call spring boot api to update cash balance
            await apiService.updateCashBalance(
                portfolioId, 
                -parseFloat(amount), 
                reason || "Cash Withdrawal"
            );

            setMessage("Cash withdrawn successfully.");
            setAmount("");
            setReason("");

            if (onCashUpdated) {
                onCashUpdated(); // Notify parent component
            }
        } catch (error) {
            console.error("Error withdrawing cash:", error);
            setMessage("Failed to withdraw cash. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return(
        <div className="card mt-4">
            <div className="card-header">
                <h5>💰 Cash Management</h5>
            </div>
            <div className="card-body">

                <div className="mb-3">
                    <label className="form-label">Amount ($)</label>
                    <input 
                        type="number" 
                        className="form-control" 
                        value={amount} 
                        onChange={(e) => setAmount(e.target.value)} 
                        placeholder="Enter amount "
                        min="0"
                        step="0.01"
                        />
                </div>

                <div className="mb-3">
                    <label className="form-label">Reason (Optional)</label>
                    <input 
                        type="number" 
                        className="form-control" 
                        value={amount} 
                        onChange={(e) => setReason(e.target.value)} 
                        placeholder="Monthly Savings, Emergency Fund, etc. "
                        />
                </div>
                /* Action Buttons */
                <div className="d-grid gap-2 d-md-flex">
                    <button
                    className="btn btn-success"
                    onClick={handleDeposit}
                    disabled = {loading}
                    >
                        {loading ? " Processing..." : " Deposit Money"}
                    </button>

                    <button
                    className="btn btn-warning"
                    onClick={handleWithdraw}
                    disabled={loading}
                    >
                        {loading ? " Processing..." : " Withdraw Money"}
                    </button>
                </div>

                {message && (
                    <div className={`alert mt-3 ${message.includes('✅') ? 'alert-success' : 'alert-danger'}`}>
            {message}
          </div>
                    
                )}
                </div>
                </div>
    );
}
export default CashManager;

    