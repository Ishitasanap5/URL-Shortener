Distributed URL Shortener

A production-grade URL shortening service built with Java 17 and Spring Boot 4. Designed for high throughput with sub-10ms redirect latency at scale, using a layered architecture: Bloom filter pre-check → Redis cache → MySQL persistence, with Kafka-backed async click analytics.


Tech Stack

LayerTechnologyLanguageJava 17FrameworkSpring Boot 4Primary DBMySQL 8CacheRedisMessagingApache KafkaID GenerationCustom SnowflakeBuildMaven


Architecture

High-Level System Design

graph TD
    Client -->|POST /shorten| API[Spring Boot API]
    Client -->|GET /{shortCode}| API

    API -->|Shorten| RLS[RateLimiterService]
    RLS -->|Sliding window\nper IP in Redis| Redis

    API -->|Shorten| USS[UrlShortenerService]
    USS -->|1. findByLongUrl| MySQL[(MySQL\nurls table)]
    USS -->|2. Snowflake ID\n+ Base62 encode| SNF[SnowflakeIdGenerator]
    USS -->|3. Save new URL| MySQL
    USS -->|4. bloomFilter.add| BF[BloomFilter\nin-memory]

    API -->|Redirect| USS
    USS -->|1. bloomFilter.mightContain| BF
    USS -->|2. Cache hit?| Redis
    USS -->|3. Cache miss → fetch + cache| MySQL

    API -->|Async| KP[KafkaProducerService]
    KP -->|click-events topic| Kafka[(Kafka)]
    Kafka -->|KafkaListener| KC[KafkaConsumerService]
    KC -->|Save ClickEvent| MySQL

    API -->|GET /analytics/:code| AQS[AnalyticsQueryService]
    AQS -->|totalClicks\ndailyClicks\nreferrers| MySQL


Shorten URL Flow

BloomFilterRedisMySQLUrlShortenerServiceRateLimitFilterClientBloomFilterRedisMySQLUrlShortenerServiceRateLimitFilterClient#mermaid-r1vi-r6 { font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; font-size: 16px; fill: rgb(25, 25, 25); }
#mermaid-r1vi-r6 .edge-animation-slow { stroke-dashoffset: 900; animation: 50s linear 0s infinite normal none running dash; stroke-linecap: round; stroke-dasharray: 9, 5 !important; }
#mermaid-r1vi-r6 .edge-animation-fast { stroke-dashoffset: 900; animation: 20s linear 0s infinite normal none running dash; stroke-linecap: round; stroke-dasharray: 9, 5 !important; }
#mermaid-r1vi-r6 .error-icon { fill: rgb(204, 120, 92); }
#mermaid-r1vi-r6 .error-text { fill: rgb(51, 135, 163); stroke: rgb(51, 135, 163); }
#mermaid-r1vi-r6 .edge-thickness-normal { stroke-width: 1px; }
#mermaid-r1vi-r6 .edge-thickness-thick { stroke-width: 3.5px; }
#mermaid-r1vi-r6 .edge-pattern-solid { stroke-dasharray: 0; }
#mermaid-r1vi-r6 .edge-thickness-invisible { stroke-width: 0; fill: none; }
#mermaid-r1vi-r6 .edge-pattern-dashed { stroke-dasharray: 3; }
#mermaid-r1vi-r6 .edge-pattern-dotted { stroke-dasharray: 2; }
#mermaid-r1vi-r6 .marker { fill: rgb(145, 145, 141); stroke: rgb(145, 145, 141); }
#mermaid-r1vi-r6 .marker.cross { stroke: rgb(145, 145, 141); }
#mermaid-r1vi-r6 svg { font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; font-size: 16px; }
#mermaid-r1vi-r6 p { margin: 0px; }
#mermaid-r1vi-r6 .actor { stroke: rgb(217, 216, 213); fill: rgb(240, 240, 235); stroke-width: 1; }
#mermaid-r1vi-r6 rect.actor.outer-path[data-look="neo"] { filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vi-r6 rect.note[data-look="neo"] { stroke: rgb(217, 216, 213); fill: rgb(240, 240, 235); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vi-r6 text.actor > tspan { fill: rgb(25, 25, 25); stroke: none; }
#mermaid-r1vi-r6 .actor-line { stroke: rgb(145, 145, 141); }
#mermaid-r1vi-r6 .innerArc { stroke-width: 1.5; stroke-dasharray: none; }
#mermaid-r1vi-r6 .messageLine0 { stroke-width: 1.5; stroke-dasharray: none; stroke: rgb(25, 25, 25); }
#mermaid-r1vi-r6 .messageLine1 { stroke-width: 1.5; stroke-dasharray: 2, 2; stroke: rgb(25, 25, 25); }
#mermaid-r1vi-r6 [id$="-arrowhead"] path { fill: rgb(25, 25, 25); stroke: rgb(25, 25, 25); }
#mermaid-r1vi-r6 .sequenceNumber { fill: rgb(110, 110, 114); }
#mermaid-r1vi-r6 [id$="-sequencenumber"] { fill: rgb(25, 25, 25); }
#mermaid-r1vi-r6 [id$="-crosshead"] path { fill: rgb(25, 25, 25); stroke: rgb(25, 25, 25); }
#mermaid-r1vi-r6 .messageText { fill: rgb(25, 25, 25); stroke: none; }
#mermaid-r1vi-r6 .labelBox { stroke: rgb(217, 216, 213); fill: rgb(240, 240, 235); filter: none; }
#mermaid-r1vi-r6 .labelText, #mermaid-r1vi-r6 .labelText > tspan { fill: rgb(25, 25, 25); stroke: none; }
#mermaid-r1vi-r6 .loopText, #mermaid-r1vi-r6 .loopText > tspan { fill: rgb(25, 25, 25); stroke: none; }
#mermaid-r1vi-r6 .loopLine { stroke-width: 2px; stroke-dasharray: 2, 2; stroke: rgb(217, 216, 213); fill: rgb(217, 216, 213); }
#mermaid-r1vi-r6 .note { stroke: rgb(217, 216, 213); fill: rgb(240, 240, 235); }
#mermaid-r1vi-r6 .noteText, #mermaid-r1vi-r6 .noteText > tspan { fill: rgb(25, 25, 25); stroke: none; font-weight: normal; }
#mermaid-r1vi-r6 .activation0 { fill: rgb(245, 230, 216); stroke: rgb(235, 204, 175); }
#mermaid-r1vi-r6 .activation1 { fill: rgb(245, 230, 216); stroke: rgb(235, 204, 175); }
#mermaid-r1vi-r6 .activation2 { fill: rgb(245, 230, 216); stroke: rgb(235, 204, 175); }
#mermaid-r1vi-r6 .actorPopupMenu { position: absolute; }
#mermaid-r1vi-r6 .actorPopupMenuPanel { position: absolute; fill: rgb(240, 240, 235); box-shadow: rgba(0, 0, 0, 0.2) 0px 8px 16px 0px; filter: drop-shadow(rgba(0, 0, 0, 0.4) 3px 5px 2px); }
#mermaid-r1vi-r6 .actor-man circle, #mermaid-r1vi-r6 line { fill: rgb(240, 240, 235); stroke-width: 2px; }
#mermaid-r1vi-r6 g rect.rect { filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); stroke: rgb(217, 216, 213); }
#mermaid-r1vi-r6 .node .neo-node { stroke: rgb(217, 216, 213); }
#mermaid-r1vi-r6 [data-look="neo"].node rect, #mermaid-r1vi-r6 [data-look="neo"].cluster rect, #mermaid-r1vi-r6 [data-look="neo"].node polygon { stroke: url("#mermaid-r1vi-r6-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vi-r6 [data-look="neo"].node path { stroke: url("#mermaid-r1vi-r6-gradient"); stroke-width: 1px; }
#mermaid-r1vi-r6 [data-look="neo"].node .outer-path { filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vi-r6 [data-look="neo"].node .neo-line path { stroke: rgb(217, 216, 213); filter: none; }
#mermaid-r1vi-r6 [data-look="neo"].node circle { stroke: url("#mermaid-r1vi-r6-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vi-r6 [data-look="neo"].node circle .state-start { fill: rgb(0, 0, 0); }
#mermaid-r1vi-r6 [data-look="neo"].icon-shape .icon { fill: url("#mermaid-r1vi-r6-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vi-r6 [data-look="neo"].icon-shape .icon-neo path { stroke: url("#mermaid-r1vi-r6-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vi-r6 :root { --mermaid-font-family: "Anthropic Sans",system-ui,"Segoe UI",Roboto,Helvetica,Arial,sans-serif; }alt[URL already exists][New URL]POST /shorten { longUrl }increment Redis counter for IP429 if > 100 req/minshortenUrl(longUrl)findByLongUrl(longUrl)existing Url entity{ shortUrl, shortCode }Snowflake.generateId()Base62.encode(id)save(Url)bloomFilter.add(shortCode){ shortUrl, shortCode }


Redirect Flow

KafkaMySQLRedisBloomFilterUrlShortenerServiceClientKafkaMySQLRedisBloomFilterUrlShortenerServiceClient#mermaid-r1vj-r7 { font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; font-size: 16px; fill: rgb(25, 25, 25); }
#mermaid-r1vj-r7 .edge-animation-slow { stroke-dashoffset: 900; animation: 50s linear 0s infinite normal none running dash; stroke-linecap: round; stroke-dasharray: 9, 5 !important; }
#mermaid-r1vj-r7 .edge-animation-fast { stroke-dashoffset: 900; animation: 20s linear 0s infinite normal none running dash; stroke-linecap: round; stroke-dasharray: 9, 5 !important; }
#mermaid-r1vj-r7 .error-icon { fill: rgb(204, 120, 92); }
#mermaid-r1vj-r7 .error-text { fill: rgb(51, 135, 163); stroke: rgb(51, 135, 163); }
#mermaid-r1vj-r7 .edge-thickness-normal { stroke-width: 1px; }
#mermaid-r1vj-r7 .edge-thickness-thick { stroke-width: 3.5px; }
#mermaid-r1vj-r7 .edge-pattern-solid { stroke-dasharray: 0; }
#mermaid-r1vj-r7 .edge-thickness-invisible { stroke-width: 0; fill: none; }
#mermaid-r1vj-r7 .edge-pattern-dashed { stroke-dasharray: 3; }
#mermaid-r1vj-r7 .edge-pattern-dotted { stroke-dasharray: 2; }
#mermaid-r1vj-r7 .marker { fill: rgb(145, 145, 141); stroke: rgb(145, 145, 141); }
#mermaid-r1vj-r7 .marker.cross { stroke: rgb(145, 145, 141); }
#mermaid-r1vj-r7 svg { font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; font-size: 16px; }
#mermaid-r1vj-r7 p { margin: 0px; }
#mermaid-r1vj-r7 .actor { stroke: rgb(217, 216, 213); fill: rgb(240, 240, 235); stroke-width: 1; }
#mermaid-r1vj-r7 rect.actor.outer-path[data-look="neo"] { filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vj-r7 rect.note[data-look="neo"] { stroke: rgb(217, 216, 213); fill: rgb(240, 240, 235); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vj-r7 text.actor > tspan { fill: rgb(25, 25, 25); stroke: none; }
#mermaid-r1vj-r7 .actor-line { stroke: rgb(145, 145, 141); }
#mermaid-r1vj-r7 .innerArc { stroke-width: 1.5; stroke-dasharray: none; }
#mermaid-r1vj-r7 .messageLine0 { stroke-width: 1.5; stroke-dasharray: none; stroke: rgb(25, 25, 25); }
#mermaid-r1vj-r7 .messageLine1 { stroke-width: 1.5; stroke-dasharray: 2, 2; stroke: rgb(25, 25, 25); }
#mermaid-r1vj-r7 [id$="-arrowhead"] path { fill: rgb(25, 25, 25); stroke: rgb(25, 25, 25); }
#mermaid-r1vj-r7 .sequenceNumber { fill: rgb(110, 110, 114); }
#mermaid-r1vj-r7 [id$="-sequencenumber"] { fill: rgb(25, 25, 25); }
#mermaid-r1vj-r7 [id$="-crosshead"] path { fill: rgb(25, 25, 25); stroke: rgb(25, 25, 25); }
#mermaid-r1vj-r7 .messageText { fill: rgb(25, 25, 25); stroke: none; }
#mermaid-r1vj-r7 .labelBox { stroke: rgb(217, 216, 213); fill: rgb(240, 240, 235); filter: none; }
#mermaid-r1vj-r7 .labelText, #mermaid-r1vj-r7 .labelText > tspan { fill: rgb(25, 25, 25); stroke: none; }
#mermaid-r1vj-r7 .loopText, #mermaid-r1vj-r7 .loopText > tspan { fill: rgb(25, 25, 25); stroke: none; }
#mermaid-r1vj-r7 .loopLine { stroke-width: 2px; stroke-dasharray: 2, 2; stroke: rgb(217, 216, 213); fill: rgb(217, 216, 213); }
#mermaid-r1vj-r7 .note { stroke: rgb(217, 216, 213); fill: rgb(240, 240, 235); }
#mermaid-r1vj-r7 .noteText, #mermaid-r1vj-r7 .noteText > tspan { fill: rgb(25, 25, 25); stroke: none; font-weight: normal; }
#mermaid-r1vj-r7 .activation0 { fill: rgb(245, 230, 216); stroke: rgb(235, 204, 175); }
#mermaid-r1vj-r7 .activation1 { fill: rgb(245, 230, 216); stroke: rgb(235, 204, 175); }
#mermaid-r1vj-r7 .activation2 { fill: rgb(245, 230, 216); stroke: rgb(235, 204, 175); }
#mermaid-r1vj-r7 .actorPopupMenu { position: absolute; }
#mermaid-r1vj-r7 .actorPopupMenuPanel { position: absolute; fill: rgb(240, 240, 235); box-shadow: rgba(0, 0, 0, 0.2) 0px 8px 16px 0px; filter: drop-shadow(rgba(0, 0, 0, 0.4) 3px 5px 2px); }
#mermaid-r1vj-r7 .actor-man circle, #mermaid-r1vj-r7 line { fill: rgb(240, 240, 235); stroke-width: 2px; }
#mermaid-r1vj-r7 g rect.rect { filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); stroke: rgb(217, 216, 213); }
#mermaid-r1vj-r7 .node .neo-node { stroke: rgb(217, 216, 213); }
#mermaid-r1vj-r7 [data-look="neo"].node rect, #mermaid-r1vj-r7 [data-look="neo"].cluster rect, #mermaid-r1vj-r7 [data-look="neo"].node polygon { stroke: url("#mermaid-r1vj-r7-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vj-r7 [data-look="neo"].node path { stroke: url("#mermaid-r1vj-r7-gradient"); stroke-width: 1px; }
#mermaid-r1vj-r7 [data-look="neo"].node .outer-path { filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vj-r7 [data-look="neo"].node .neo-line path { stroke: rgb(217, 216, 213); filter: none; }
#mermaid-r1vj-r7 [data-look="neo"].node circle { stroke: url("#mermaid-r1vj-r7-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vj-r7 [data-look="neo"].node circle .state-start { fill: rgb(0, 0, 0); }
#mermaid-r1vj-r7 [data-look="neo"].icon-shape .icon { fill: url("#mermaid-r1vj-r7-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vj-r7 [data-look="neo"].icon-shape .icon-neo path { stroke: url("#mermaid-r1vj-r7-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vj-r7 :root { --mermaid-font-family: "Anthropic Sans",system-ui,"Segoe UI",Roboto,Helvetica,Arial,sans-serif; }alt[Cache HIT][Cache MISS]alt[Bloom filter miss (definitely not exists)][Bloom filter pass]GET /{shortCode}mightContain(shortCode)false404 Not Foundtrueget("url:" + shortCode)longUrlfindByShortCode(shortCode)Url entityset("url:" + shortCode, longUrl, TTL=7d)302 Redirect → longUrlsendClickEvent (async, non-blocking)


Snowflake ID Structure

 63        22        12        0
 |---------|---------|---------|
 41 bits   10 bits   12 bits
 timestamp machineId sequence


41 bits timestamp (ms since custom epoch Jan 1 2024) → ~69 years of IDs
10 bits machine ID → 1024 unique nodes
12 bits sequence → 4096 IDs per millisecond per node
Total throughput: ~4M IDs/sec across 1024 nodes, zero coordination required



Bloom Filter

Pre-check layer before Redis and MySQL on every redirect. Eliminates DB hits for non-existent short codes.

mightContain(shortCode)
    → hash1(code) = (hashCode & 0x7fff) % size
    → hash2(code) = (hashCode ^ hashCode>>>16 & 0x7fff) % size
    → hash3(code) = (h1 + h2) % size
    → returns bitSet[h1] && bitSet[h2] && bitSet[h3]

ParameterValueBit array size1,000,000 bits (~125 KB)Hash functions3False positive rate< 1% at low cardinalityStartup hydrationLoaded from DB on ApplicationRunnerThread safetyadd() synchronized


Rate Limiter

Sliding window counter per IP using Redis atomic INCR + EXPIRE.

key = "rate_limit:{ip}"
count = INCR key
if count == 1 → EXPIRE key 60s
if count > 100 → return 429

Window resets after 60s from first request in that window. Handles X-Forwarded-For for clients behind proxies.


Kafka Click Analytics Pipeline

#mermaid-r1vn-r8 { font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; font-size: 16px; fill: rgb(25, 25, 25); }
#mermaid-r1vn-r8 .edge-animation-slow { stroke-dashoffset: 900; animation: 50s linear 0s infinite normal none running dash; stroke-linecap: round; stroke-dasharray: 9, 5 !important; }
#mermaid-r1vn-r8 .edge-animation-fast { stroke-dashoffset: 900; animation: 20s linear 0s infinite normal none running dash; stroke-linecap: round; stroke-dasharray: 9, 5 !important; }
#mermaid-r1vn-r8 .error-icon { fill: rgb(204, 120, 92); }
#mermaid-r1vn-r8 .error-text { fill: rgb(51, 135, 163); stroke: rgb(51, 135, 163); }
#mermaid-r1vn-r8 .edge-thickness-normal { stroke-width: 1px; }
#mermaid-r1vn-r8 .edge-thickness-thick { stroke-width: 3.5px; }
#mermaid-r1vn-r8 .edge-pattern-solid { stroke-dasharray: 0; }
#mermaid-r1vn-r8 .edge-thickness-invisible { stroke-width: 0; fill: none; }
#mermaid-r1vn-r8 .edge-pattern-dashed { stroke-dasharray: 3; }
#mermaid-r1vn-r8 .edge-pattern-dotted { stroke-dasharray: 2; }
#mermaid-r1vn-r8 .marker { fill: rgb(145, 145, 141); stroke: rgb(145, 145, 141); }
#mermaid-r1vn-r8 .marker.cross { stroke: rgb(145, 145, 141); }
#mermaid-r1vn-r8 svg { font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; font-size: 16px; }
#mermaid-r1vn-r8 p { margin: 0px; }
#mermaid-r1vn-r8 .label { font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; color: rgb(25, 25, 25); }
#mermaid-r1vn-r8 .cluster-label text { fill: rgb(51, 135, 163); }
#mermaid-r1vn-r8 .cluster-label span { color: rgb(51, 135, 163); }
#mermaid-r1vn-r8 .cluster-label span p { background-color: transparent; }
#mermaid-r1vn-r8 .label text, #mermaid-r1vn-r8 span { fill: rgb(25, 25, 25); color: rgb(25, 25, 25); }
#mermaid-r1vn-r8 .node rect, #mermaid-r1vn-r8 .node circle, #mermaid-r1vn-r8 .node ellipse, #mermaid-r1vn-r8 .node polygon, #mermaid-r1vn-r8 .node path { fill: rgb(240, 240, 235); stroke: rgb(217, 216, 213); stroke-width: 1px; }
#mermaid-r1vn-r8 .rough-node .label text, #mermaid-r1vn-r8 .node .label text, #mermaid-r1vn-r8 .image-shape .label, #mermaid-r1vn-r8 .icon-shape .label { text-anchor: middle; }
#mermaid-r1vn-r8 .node .katex path { fill: rgb(0, 0, 0); stroke: rgb(0, 0, 0); stroke-width: 1px; }
#mermaid-r1vn-r8 .rough-node .label, #mermaid-r1vn-r8 .node .label, #mermaid-r1vn-r8 .image-shape .label, #mermaid-r1vn-r8 .icon-shape .label { text-align: center; }
#mermaid-r1vn-r8 .node.clickable { cursor: pointer; }
#mermaid-r1vn-r8 .root .anchor path { stroke-width: 0; stroke: rgb(145, 145, 141); fill: rgb(145, 145, 141) !important; }
#mermaid-r1vn-r8 .arrowheadPath { fill: rgb(11, 11, 11); }
#mermaid-r1vn-r8 .edgePath .path { stroke: rgb(145, 145, 141); stroke-width: 1px; }
#mermaid-r1vn-r8 .flowchart-link { stroke: rgb(145, 145, 141); fill: none; }
#mermaid-r1vn-r8 .edgeLabel { background-color: rgb(245, 230, 216); text-align: center; }
#mermaid-r1vn-r8 .edgeLabel p { background-color: rgb(245, 230, 216); }
#mermaid-r1vn-r8 .edgeLabel rect { opacity: 0.5; background-color: rgb(245, 230, 216); fill: rgb(245, 230, 216); }
#mermaid-r1vn-r8 .labelBkg { background-color: rgba(245, 230, 216, 0.5); }
#mermaid-r1vn-r8 .cluster rect { fill: rgb(204, 120, 92); stroke: rgb(138, 115, 107); stroke-width: 1px; }
#mermaid-r1vn-r8 .cluster text { fill: rgb(51, 135, 163); }
#mermaid-r1vn-r8 .cluster span { color: rgb(51, 135, 163); }
#mermaid-r1vn-r8 div.mermaidTooltip { position: absolute; text-align: center; max-width: 200px; padding: 2px; font-family: "Anthropic Sans", system-ui, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; font-size: 12px; background: rgb(204, 120, 92); border: 1px solid rgb(138, 115, 107); border-radius: 2px; pointer-events: none; z-index: 100; }
#mermaid-r1vn-r8 .flowchartTitleText { text-anchor: middle; font-size: 18px; fill: rgb(25, 25, 25); }
#mermaid-r1vn-r8 rect.text { fill: none; stroke-width: 0; }
#mermaid-r1vn-r8 .icon-shape, #mermaid-r1vn-r8 .image-shape { background-color: rgb(245, 230, 216); text-align: center; }
#mermaid-r1vn-r8 .icon-shape p, #mermaid-r1vn-r8 .image-shape p { background-color: rgb(245, 230, 216); padding: 2px; }
#mermaid-r1vn-r8 .icon-shape .label rect, #mermaid-r1vn-r8 .image-shape .label rect { opacity: 0.5; background-color: rgb(245, 230, 216); fill: rgb(245, 230, 216); }
#mermaid-r1vn-r8 .label-icon { display: inline-block; height: 1em; overflow: visible; vertical-align: -0.125em; }
#mermaid-r1vn-r8 .node .label-icon path { fill: currentcolor; stroke: revert; stroke-width: revert; }
#mermaid-r1vn-r8 .node .neo-node { stroke: rgb(217, 216, 213); }
#mermaid-r1vn-r8 [data-look="neo"].node rect, #mermaid-r1vn-r8 [data-look="neo"].cluster rect, #mermaid-r1vn-r8 [data-look="neo"].node polygon { stroke: url("#mermaid-r1vn-r8-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vn-r8 [data-look="neo"].node path { stroke: url("#mermaid-r1vn-r8-gradient"); stroke-width: 1px; }
#mermaid-r1vn-r8 [data-look="neo"].node .outer-path { filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vn-r8 [data-look="neo"].node .neo-line path { stroke: rgb(217, 216, 213); filter: none; }
#mermaid-r1vn-r8 [data-look="neo"].node circle { stroke: url("#mermaid-r1vn-r8-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vn-r8 [data-look="neo"].node circle .state-start { fill: rgb(0, 0, 0); }
#mermaid-r1vn-r8 [data-look="neo"].icon-shape .icon { fill: url("#mermaid-r1vn-r8-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vn-r8 [data-look="neo"].icon-shape .icon-neo path { stroke: url("#mermaid-r1vn-r8-gradient"); filter: drop-shadow(rgb(185, 185, 185) 1px 2px 2px); }
#mermaid-r1vn-r8 :root { --mermaid-font-family: "Anthropic Sans",system-ui,"Segoe UI",Roboto,Helvetica,Arial,sans-serif; }non-blockingtopic: click-eventskey: shortCodegroup: analytics-grouppersistaggregation queriesGET /shortCodeKafkaProducerServiceKafka BrokerKafkaConsumerServiceclick_events tableGET /analytics/shortCode

Analytics decoupled from the redirect hot path — redirect latency is never blocked by DB writes.


Database Schema

sqlCREATE TABLE urls (
    id          BIGINT PRIMARY KEY,          -- Snowflake ID
    short_code  VARCHAR(10)  NOT NULL UNIQUE,
    long_url    TEXT         NOT NULL,
    created_at  TIMESTAMP,
    version     BIGINT,                      -- optimistic locking
    INDEX idx_long_url (long_url(255))       -- deduplication lookup
);

CREATE TABLE click_events (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code  VARCHAR(10),
    clicked_at  TIMESTAMP,
    ip_address  VARCHAR(45),
    user_agent  TEXT,
    referrer    TEXT,
    INDEX idx_short_code (short_code)        -- analytics queries
);


API Reference

POST /shorten

Shorten a long URL. Idempotent — same long URL always returns the same short code.

Request

json{ "longUrl": "https://example.com/very/long/path" }

Response 200 OK

json{
  "shortUrl": "http://localhost:8080/aB3xYz",
  "shortCode": "aB3xYz"
}

Errors


400 — blank URL or invalid URL format
429 — rate limit exceeded (> 100 req/min per IP)



GET /{shortCode}

Redirect to the original URL. Fires an async Kafka click event.

Response 302 Found → Location: <originalUrl>

Errors


404 — short code not found



GET /analytics/{shortCode}

Click analytics for a short URL.

Response 200 OK

json{
  "totalClicks": 1482,
  "dailyClicks": [
    { "date": "2024-06-01", "clicks": 312 },
    { "date": "2024-06-02", "clicks": 405 }
  ],
  "referrers": [
    { "referrer": "direct", "clicks": 890 },
    { "referrer": "https://twitter.com", "clicks": 592 }
  ]
}


Running Locally

Prerequisites: Java 17, Maven, MySQL 8, Redis, Kafka

bash# 1. Clone
git clone https://github.com/Ishitasanap5/URL-Shortener.git
cd URL-Shortener

# 2. Create DB
mysql -u root -p -e "CREATE DATABASE urlshortener;"

# 3. Configure
# src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/urlshortener
spring.datasource.username=<your_username>
spring.datasource.password=<your_password>
app.base-url=http://localhost:8080/

# 4. Start Kafka (if using Docker)
docker run -d -p 9092:9092 apache/kafka:3.7.0

# 5. Run
mvn spring-boot:run


Design Decisions

Why Snowflake IDs over UUID?
UUIDs are random → cause B-tree index fragmentation on insert-heavy workloads. Snowflake IDs are monotonically increasing → sequential inserts, better index locality, smaller storage (8 bytes vs 16 bytes).

Why Bloom filter before Redis?
Redirect requests for non-existent short codes (typos, scraping) would otherwise hit Redis first, then MySQL on a miss. The in-memory Bloom filter eliminates this entire class of lookups at the cost of ~125KB RAM and a guaranteed-false response.

Why Kafka for click analytics?
Synchronous DB writes on the redirect path would add 2–5ms latency per redirect under load. Kafka decouples the hot path from analytics persistence entirely. Click events are durable in Kafka and written to MySQL asynchronously by the consumer group.

Why cache-aside over write-through?
Write-through caches every new URL immediately. Cache-aside only populates Redis on first access, keeping memory usage proportional to actual traffic patterns rather than total URL count.

Why Base62 over MD5/SHA hash?
Hash-based approaches can collide and produce codes longer than needed. Base62 encoding of a Snowflake ID produces a deterministic, collision-free 7–8 character code (62^7 ≈ 3.5 trillion combinations).
