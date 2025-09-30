import { useState } from "react";

export default function Signup({setCurrentScreen}) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
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

  const handleSignup = async (e) => {
    e.preventDefault();

    const passwordError = validatePassword(password);
    if (passwordError) {
      setMessage("❌ " + passwordError);
      return;
    }

    try {
      const response = await fetch("https://rgbradford-app.onrender.com/api/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();

      if (response.ok) {
        if (data.accessToken) {
          localStorage.setItem("token", data.accessToken);
          setMessage("✅ Account created");
        } else {
          setMessage("✅ Account created. Please log in.");
        }
      } 
      else {
        setMessage(data.message || "❌ Signup failed.");
      }
    } catch (error) {
      console.error("Error:", error);
      setMessage("⚠️ Server error. Try again later.");
    }
  };

  return (
    <div className="flex flex-col gap-4 h-[50vh] items-center justify-center text-base">
      <form
        onSubmit={handleSignup}
        className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-6 shadow-md"
      >
        <h1 className="text-2xl font-semibold text-gray-800 ">Sign Up</h1>

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

        <p className="text-xs text-gray-600 mt-1">
          Password must be at least 5 characters and contain a letter, number, and special character
        </p>

        <button type="submit" className="btn !bg-green-400 text-base">
          Create Account
        </button>

        {message && (
          <p className="text-center text-sm text-gray-600">{message}</p>
        )}
      </form>
        <p className="cursor-pointer !text-white hover:underline text-base"
        onClick={() => setCurrentScreen('login')}>
          Already have an account? Log in!
        </p>
    </div>
  );
}
