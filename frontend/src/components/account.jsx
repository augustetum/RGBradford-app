import React, { useState } from "react";
import { API_BASE_URL } from "../config";

function Account({data, onLogout}) {
    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [message, setMessage] = useState("");

    const validatePassword = (password) => {
        if (password.length < 5) {
            return "Password must be at least 5 characters long";
        }
        if (!/[a-zA-Z]/.test(password)) {
            return "Password must contain at least one letter";
        }
        if (!/[0-9]/.test(password)) {
            return "Password must contain at least one number";
        }
        if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
            return "Password must contain at least one special character";
        }
        return null;
    };

    const handleChangePassword = async (e) => {
        e.preventDefault();

        if (newPassword !== confirmPassword) {
            setMessage("❌ New passwords don't match");
            return;
        }

        if (newPassword === oldPassword) {
          setMessage("❌ New password cannot be the same as the old password");
          return;
        }

        const passwordError = validatePassword(newPassword);
        if (passwordError) {
            setMessage("❌ " + passwordError);
            return;
        }

        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/auth/change-password`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify({ oldPassword, newPassword }),
            });

            const data = await response.json();

            if (response.ok) {
                setMessage("✅ Password changed successfully");
                setOldPassword("");
                setNewPassword("");
                setConfirmPassword("");
            } else {
                setMessage("❌ " + (data.message || "Failed to change password"));
            }
        } catch (error) {
            console.error("Error:", error);
            setMessage("⚠️ Server error. Try again later.");
        }
    };

    return (
        <div className="text-base flex flex-col gap-6 max-w-md mx-auto">

            <form
                onSubmit={handleChangePassword}
                className="w-full space-y-4 rounded-2xl bg-white p-6 shadow-md"
            >
                <h2 className="text-2xl font-semibold text-gray-800">Change Password</h2>

                <input
                    type="password"
                    placeholder="Old Password"
                    value={oldPassword}
                    onChange={(e) => setOldPassword(e.target.value)}
                    className="inpt !w-full"
                    required
                />

                <input
                    type="password"
                    placeholder="New Password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className="inpt !w-full"
                    required
                />

                <input
                    type="password"
                    placeholder="Confirm New Password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="inpt !w-full"
                    required
                />

                <p className="text-xs text-gray-600 mt-1">
                    Password must be at least 5 characters and contain a letter, number, and special character
                </p>

                <button type="submit" className="btn  !bg-gray-300">
                    Change Password
                </button>

                {message && (
                    <p className="text-center text-sm text-gray-600">{message}</p>
                )}
            </form>

            <button
                onClick={onLogout}
                className="btn  !bg-igem-red w-full"
            >
                Log Out
            </button>
        </div>
    )
}
export default Account