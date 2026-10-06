import { useState } from "react";
import { getAnalytics } from "../api/urlApi";

export default function Analytics() {
  const [shortCode, setShortCode] = useState("");
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const fetchAnalytics = async () => {
    if (!shortCode.trim()) return;

    try {
      setLoading(true);
      setError("");
      setData(null);
      const res = await getAnalytics(shortCode.trim());
      setData(res);
    } catch (e) {
      setError(e.message || "Could not fetch analytics");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-60px)] flex items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-blue-50 px-4 py-8">
      <div className="w-full max-w-2xl space-y-6">

        {/* Header */}
        <div className="text-center">
          <h1 className="text-3xl font-bold text-gray-800">Analytics Dashboard</h1>
          <p className="text-gray-500 mt-1">Enter your short code to view insights</p>
        </div>

        {/* Input Card */}
        <div className="bg-white shadow-md rounded-2xl p-3 flex gap-3 border border-gray-100">
          <input
            className="flex-1 px-4 outline-none text-gray-700"
            placeholder="e.g. aB12cd"
            value={shortCode}
            onChange={(e) => setShortCode(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && fetchAnalytics()}
          />

          <button
            onClick={fetchAnalytics}
            disabled={loading}
            className="px-5 py-2.5 rounded-xl bg-black text-white hover:opacity-90 transition font-medium disabled:opacity-50"
          >
            {loading ? "Loading..." : "View"}
          </button>
        </div>

        {/* Error */}
        {error && (
          <div className="text-center text-red-500 text-sm">
            {error}
          </div>
        )}

        {/* Data */}
        {data && (
          <div className="space-y-4">
            
            {/* Stats cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="bg-white rounded-2xl p-5 shadow-sm border">
                <p className="text-gray-500 text-sm">Total Clicks</p>
                <p className="text-2xl font-bold text-gray-800">{data.totalClicks}</p>
              </div>

              <div className="bg-white rounded-2xl p-5 shadow-sm border">
                <p className="text-gray-500 text-sm">Days Tracked</p>
                <p className="text-2xl font-bold text-gray-800">{data.dailyClicks.length}</p>
              </div>

              <div className="bg-white rounded-2xl p-5 shadow-sm border">
                <p className="text-gray-500 text-sm">Referrers</p>
                <p className="text-2xl font-bold text-gray-800">{data.referrerStats.length}</p>
              </div>
            </div>

            {/* Daily clicks */}
            <div className="bg-white rounded-2xl p-5 shadow-sm border">
              <h2 className="font-semibold text-gray-800 mb-3">Daily Activity</h2>
              {data.dailyClicks.length === 0 ? (
                <p className="text-sm text-gray-400">No daily click records yet.</p>
              ) : (
                <div className="space-y-2">
                  {data.dailyClicks.map((d, i) => (
                    <div key={i} className="flex justify-between text-sm text-gray-600">
                      <span>{d.date}</span>
                      <span className="font-medium">{d.count} clicks</span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Referrers */}
            <div className="bg-white rounded-2xl p-5 shadow-sm border">
              <h2 className="font-semibold text-gray-800 mb-3">Traffic Sources</h2>
              {data.referrerStats.length === 0 ? (
                <p className="text-sm text-gray-400">No referrer stats available.</p>
              ) : (
                <div className="space-y-2">
                  {data.referrerStats.map((r, i) => (
                    <div key={i} className="flex justify-between text-sm text-gray-600">
                      <span className="truncate max-w-[70%]">{r.referrer}</span>
                      <span className="font-medium">{r.count}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>

          </div>
        )}

      </div>
    </div>
  );
}