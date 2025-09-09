import { useState } from "react";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch("https://rgbradford-app.onrender.com/api/auth/login", {
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
      } else {
        setMessage(data.message || "❌ Login failed.");
      }
    } catch (error) {
      console.error("Error:", error);
      setMessage("⚠️ Server error. Try again later.");
    }
  };

  return (
    <div className="flex h-[50vh] items-center justify-center">
      <form
        onSubmit={handleLogin}
        className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-6 shadow-md"
      >
        <h1 className="text-2xl font-semibold text-gray-800">Login</h1>

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

        <button type="submit" className="btn !bg-green-400">
          Login
        </button>

        {message && (
          <p className="text-center text-sm text-gray-600">{message}</p>
        )}
      </form>
    </div>
  );
}
