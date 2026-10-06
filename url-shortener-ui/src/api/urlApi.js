const BASE_URL = "http://localhost:8080";

export async function shortenUrl(longUrl) {
  const res = await fetch(`${BASE_URL}/shorten`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ longUrl }),
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => null);
    throw new Error(errorData?.message || "Failed to shorten URL");
  }

  return res.json();
}

export async function getAnalytics(shortCode) {
  const res = await fetch(`${BASE_URL}/analytics/${shortCode}`);

  if (!res.ok) {
    const errorData = await res.json().catch(() => null);
    throw new Error(errorData?.message || "Failed to fetch analytics");
  }

  return res.json();
}