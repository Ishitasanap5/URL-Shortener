import { useState } from "react";
import { shortenUrl } from "../api/urlApi";

export default function Home() {
  const [url, setUrl] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);

  const handleShorten = async () => {
    if (!url) return;

    try {
      setLoading(true);
      setError("");
      setResult(null);
      setCopied(false);

      const data = await shortenUrl(url);
      setResult(data);
    } catch (e) {
      setError(e.message || "Failed to shorten URL. Try again.");
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = () => {
    if (result?.shortUrl) {
      navigator.clipboard.writeText(result.shortUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="min-h-[calc(100vh-60px)] flex items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-blue-50 px-4">
      <div className="w-full max-w-xl space-y-6">
        
        {/* Header */}
        <div className="text-center">
          <h1 className="text-4xl font-bold text-gray-800">URL Shortener</h1>
          <p className="text-gray-500 mt-2">Fast • Scalable • Event-driven system</p>
        </div>

        {/* Main Card */}
        <div className="bg-white shadow-lg border border-gray-100 rounded-2xl p-6 space-y-4">
          <input
            className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-400 text-gray-700"
            placeholder="Paste your long URL here..."
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleShorten()}
          />

          <button
            onClick={handleShorten}
            disabled={loading}
            className="w-full py-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold transition disabled:opacity-50"
          >
            {loading ? "Shortening..." : "Shorten URL"}
          </button>

          {error && (
            <p className="text-center text-sm text-red-500">{error}</p>
          )}
        </div>

        {/* Result Card */}
        {result && (
          <div className="bg-white shadow-md border border-gray-100 rounded-2xl p-5 space-y-3">
            <p className="text-sm text-gray-500">Your short link</p>
            <div className="flex items-center justify-between gap-3">
              <a
                href={result.shortUrl}
                target="_blank"
                rel="noreferrer"
                className="text-indigo-600 break-all font-medium hover:underline"
              >
                {result.shortUrl}
              </a>

              <button
                onClick={copyToClipboard}
                className="px-3 py-1.5 text-sm bg-gray-100 hover:bg-gray-200 rounded-lg font-medium transition"
              >
                {copied ? "Copied!" : "Copy"}
              </button>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}