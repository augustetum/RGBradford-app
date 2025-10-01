import { useState } from "react";
import { API_BASE_URL } from "../config";

export default function Login({setCurrentScreen, setIsAuthenticated}) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();

      if (response.ok && data.accessToken) {
        localStorage.setItem("token", data.accessToken);
        setMessage("✅ Login successful!");
        setIsAuthenticated(true);
        setCurrentScreen('catalog');
      } else {
        setMessage(data.message || "❌ Login failed.");
      }
    } catch (error) {
      console.error("Error:", error);
      setMessage("⚠️ Server error. Try again later.");
    }
    }


  return (
    <div className="flex flex-col gap-4 h-[50vh] items-center justify-center text-base">
      <form
        onSubmit={handleLogin}
        className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-6 shadow-md"
      >
        <h1 className="text-2xl font-semibold text-gray-800 text-base">Login</h1>

        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="inpt !w-full"
          required
        />

        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="inpt !w-full"
          required
        />

        <button type="submit" className="btn !bg-green-400 text-base">
          Login
        </button>

        {message && (
          <p className="text-center text-sm text-gray-600 text-base">{message}</p>
        )}
      </form>
        <p className="cursor-pointer !text-white hover:underline text-base" onClick={() => setCurrentScreen('signup')}>Don't have an account? Sign up!</p>

    </div>
  );
}
