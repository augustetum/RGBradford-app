export default function Signup() {
  return (
    <div className="flex h-[50vh] items-center justify-center">
      <form className="w-full max-w-sm space-y-4 rounded-2xl bg-white p-6 shadow-md">
        <h1 className="text-2xl font-semibold text-gray-800">Sign Up</h1>
        <input
          type="text"
          placeholder="Name"
          className="inpt !w-full"
        />
        <input
          type="email"
          placeholder="Email"
          className="inpt !w-full"
        />
        <input
          type="password"
          placeholder="Password"
          className="inpt !w-full"
        />
        <button
          type="submit"
          className="btn !bg-green-400"
        >
          Create Account
        </button>
      </form>
    </div>
  );
}
