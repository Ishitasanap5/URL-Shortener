import Home from "./pages/Home";
import Analytics from "./pages/Analytics";
import { useState } from "react";

export default function App() {
  const [page, setPage] = useState("home");

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Nav */}
      <nav className="flex justify-center gap-6 p-4 bg-black text-white shadow-md">
        <button
          onClick={() => setPage("home")}
          className={`transition font-medium ${page === "home" ? "text-indigo-400 underline" : "hover:text-gray-300"}`}
        >
          Shorten
        </button>
        <button
          onClick={() => setPage("analytics")}
          className={`transition font-medium ${page === "analytics" ? "text-indigo-400 underline" : "hover:text-gray-300"}`}
        >
          Analytics
        </button>
      </nav>

      {page === "home" ? <Home /> : <Analytics />}
    </div>
  );
}