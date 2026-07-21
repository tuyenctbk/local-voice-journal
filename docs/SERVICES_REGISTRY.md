# Services Registry & Backup Strategy

This document maps all integrated APIs to their primary, secondary, and offline fallback providers.

---

## 1. AI, Machine Learning & NLP
| Category | Primary Provider | Secondary Provider | Offline Fallback |
| :--- | :--- | :--- | :--- |
| **LLM / Reasoning** | Google Gemini | Groq API (Llama 3) | Pre-scripted / LiteRT |
| **Speech-to-Text** | Google Cloud STT | AssemblyAI | ML Kit (On-Device) |
| **Text-to-Speech** | Cloud TTS | Play.ht | `flutter_tts` (Local) |
| **Translation** | **DeepL API** | Google Translate | Local Dictionary |

## 2. Infrastructure & Data (Firebase Bedrock)
| Category | Primary Provider | Secondary Provider | Offline Fallback |
| :--- | :--- | :--- | :--- |
| **Auth & Sync** | **Firebase Auth** | Supabase Auth | Device UUID (Local) |
| **- Google Auth** | Google SDK | Credential Manager | - |
| **- Facebook Auth** | Facebook Device Flow | Standard OAuth | - |
| **- Apple Auth** | Apple ID (Android Web) | - | - |
| **Database** | **Firestore** | Supabase (Postgres) | Hive / SQLite |
| **Analytics** | **Firebase Analytics** | PostHog | Local Log Buffer |
| **Storage** | **Firebase Storage** | Cloudinary | Local Asset Bundle |

## 3. Engagement & Monetization (AdMob & RC)
| Category | Primary Provider | Secondary Provider | Strategy |
| :--- | :--- | :--- | :--- |
| **Ads** | **AdMob** | Unity Ads | Postpone on fail |
| **Remote Config** | **Firebase RC** | Appflowy | Last Known Good (LKG) |
| **Push Alerts** | **FCM** | OneSignal | Local Notifications |

---

## 4. Master API Inventory (Reference)

### 4.1 AI, Machine Learning & NLP
1. **Google Gemini API** (https://ai.google.dev/) — Multimodal AI (text, images, video, code) with generous free usage via Google AI Studio.
2. **Groq API** (https://groq.com/) — Extremely fast LPU-powered AI inference supporting Llama, Mixtral, and Gemma models.
3. **Hugging Face Inference API** (https://huggingface.co/inference-api) — Access thousands of open-source ML models for NLP, vision, and audio.
4. **OpenAI API** (https://platform.openai.com/) — Benchmark LLMs and DALL-E image generation (free starter credits on signup).
5. **SiliconFlow** (https://www.siliconflow.com/) — Unified cloud API platform to run, fine-tune, and deploy open-source models.
6. **Cohere API** (https://cohere.com/) — Enterprise NLP models specialized for text generation, embeddings, and reranking.
7. **AssemblyAI** (https://www.assemblyai.com/) — Speech-to-text, audio intelligence, and speaker diarization APIs.
8. **DeepL API** (https://www.deepl.com/pro-api) — High-accuracy machine translation API (500k free chars/month).
9. **Replicate** (https://replicate.com/) — Run open-source AI models in the cloud via standard HTTP requests.
10. **Wit.ai** (https://wit.ai/) — Meta's free natural language processing platform for voice and chatbot interactions.

### 4.2 Cloud, BaaS & Developer Platforms
11. **Firebase** (https://firebase.google.com/) — Google's suite for Firestore, Authentication, Cloud Messaging, and Hosting.
12. **Supabase** (https://supabase.com/) — Open-source Firebase alternative based on PostgreSQL with realtime subscriptions and Auth.
13. **GitHub REST / GraphQL API** (https://docs.github.com/en/rest) — Query repository data, commit histories, pull requests, and profiles.
14. **Clerk** (https://clerk.com/) — Complete user management and authentication API with prebuilt UI components.
15. **Auth0** (https://auth0.com/) — Identity management, SSO, and user authentication platform.
16. **Appwrite** (https://appwrite.io/) — Open-source backend-as-a-service for Web, Mobile, and Flutter developers.
17. **Vercel API** (https://vercel.com/docs/rest-api) — Programmatically deploy, manage domains, and control cloud infrastructure.
18. **Cloudflare Workers / API** (https://developers.cloudflare.com/) — Deploy serverless edge functions and manage DNS/CDN settings.
19. **Upstash** (https://upstash.com/) — Serverless Redis, Kafka, and Vector databases with HTTP REST APIs.
20. **PlanetScale** (https://planetscale.com/) — Serverless MySQL-compatible database platform built on Vitess.

### 4.3 Maps, Location & Geolocation
21. **OpenStreetMap / Nominatim** (https://nominatim.org/) — Free open geocoding and reverse geocoding data without strict limits.
22. **Mapbox** (https://www.mapbox.com/) — Custom maps, directions, vector tiles, and spatial analysis endpoints.
23. **IPinfo** (https://ipinfo.io/) — IP geolocation API supplying ISP, city, country, and ASN details.
24. **ipapi** (https://ipapi.co/) — Quick REST IP lookup API returning location, currency, and timezone details.
25. **Google Maps Platform** (https://developers.google.com/maps) — Industry standard map rendering, places, and routing ($200 free monthly credit).
26. **Geoapify** (https://www.geoapify.com/) — Geocoding, place search, routing, and map tile REST services.
27. **Positionstack** (https://positionstack.com/) — Forward and reverse geocoding REST API with global coverage.
28. **Rest Countries** (https://restcountries.com/) — Free REST endpoint providing metadata, flags, and currencies for all world countries.
29. **Abstract IP Geolocation API** (https://www.abstractapi.com/api/ip-geolocation-api) — Look up user location and timezone from IP address.
30. **Radar** (https://radar.com/) — Geofencing, location tracking, and geocoding platform for mobile apps.

### 4.4 Media, Images & Design Tools
31. **Unsplash API** (https://unsplash.com/developers) — Access millions of high-resolution royalty-free stock photos.
32. **Pexels API** (https://www.pexels.com/api/) — Free stock photo and video retrieval API.
33. **Cloudinary** (https://cloudinary.com/) — Image and video optimization, transformation, and asset management API.
34. **Pixabay API** (https://pixabay.com/api/docs/) — Search over 2.7M free photos, vectors, illustrations, and videos.
35. **Imgur API** (https://api.imgur.com/) — Upload, fetch, and structure web-hosted image galleries.
36. **Giphy API** (https://developers.giphy.com/) — Search, display, and share animated GIF clips and stickers.
37. **DummyImage / Placeholder.com** (https://placeholder.com/) — Dynamic URL-based placeholder image generator service.
38. **QR Code Generator API** (https://goqr.me/api/) — Generate custom QR code image assets dynamically via URL parameters.
39. **Gravatar API** (https://en.gravatar.com/site/implement/) — Retrieve globally recognized user avatar images via hashed emails.
40. **Iconify API** (https://iconify.design/docs/api/) — Access over 100,000 vector icons on-demand.

### 4.5 Communications, Email & Messaging
41. **Twilio** (https://www.twilio.com/) — Programmable SMS, WhatsApp messaging, and Voice call capabilities.
42. **Resend** (https://resend.com/) — Developer-first email API for transactional emails using modern stacks.
43. **SendGrid** (https://sendgrid.com/) — Reliable transactional email delivery, templates, and analytics.
44. **Postmark** (https://postmarkapp.com/) — Ultra-fast transactional email API service (100 free emails/month).
45. **Telegram Bot API** (https://core.telegram.org/bots/api) — Complete control API to build bots, integrations, and automated notifications.
46. **Discord Webhooks / API** (https://discord.com/developers/docs/intro) — Send alerts, build bots, and automate community actions.
47. **Slack API** (https://api.slack.com/) — Custom bot integrations, interactive messages, and workspace automation.
48. **Mailchimp API** (https://mailchimp.com/developer/) — Manage newsletter subscribers, campaign workflows, and audience lists.
49. **Pusher** (https://pusher.com/) — Hosted WebSockets for real-time pub/sub messaging and notifications.
50. **Brevo (formerly Sendinblue)** (https://www.brevo.com/) — Transactional email delivery (300 free emails/day).

### 4.6 Financial, Currency & E-Commerce
51. **ExchangeRate-API** (https://www.exchangerate-api.com/) — Accurate exchange rates and currency conversion calculations.
52. **CoinGecko API** (https://www.coingecko.com/en/api) — Cryptocurrency prices, volume, market caps, and historical tracking.
53. **Stripe API** (https://stripe.com/docs/api) — Payment processing, subscription handling, and invoicing primitives.
54. **Plaid** (https://plaid.com/) — Secure connection API linking bank accounts to financial apps (free Sandbox tier).
55. **Frankfurter API** (https://www.frankfurter.app/) — Open-source currency exchange rate API tracking European Central Bank data.
56. **Finnhub** (https://finnhub.io/) — Real-time stock market quotes, financial fundamentals, and market news.
57. **Alpha Vantage** (https://www.alphavantage.co/) — Real-time and historical stock, forex, and cryptocurrency market data.
58. **CoinCap API** (https://coincap.io/) — Real-time market metrics for hundreds of cryptocurrencies.
59. **Open Exchange Rates** (https://openexchangerates.org/) — Standard forex exchange rate data service.
60. **TaxJar API** (https://www.taxjar.com/) — Real-time sales tax calculation by location.

### 4.7 Weather, Environment & Science
61. **OpenWeatherMap** (https://openweathermap.org/api) — Current weather conditions, 5-day forecasts, and air pollution indexes.
62. **Open-Meteo** (https://open-meteo.com/) — Free open-source weather API providing forecast models without requiring an API key.
63. **WeatherAPI** (https://www.weatherapi.com/) — Weather forecasts, air quality, sports events, and historical weather lookup.
64. **NASA Open APIs** (https://api.nasa.gov/) — Access astronomy picture of the day (APOD), satellite imagery, and asteroid tracking data.
65. **USGS Earthquake API** (https://earthquake.usgs.gov/fdsnws/event/1/) — Real-time seismic event tracking and historical earthquake logs.
66. **AirVisual API** (https://www.iqair.com/air-pollution-data-api) — Global air quality index (AQI) and atmospheric data monitoring.
67. **Sunrise-Sunset API** (https://sunrise-sunset.org/api) — Calculate precise sunrise and sunset times for given geographical coordinates.
68. **Launch Library 2** (https://thespacedevs.com/llapi) — Comprehensive information on spaceflight rocket launches and missions.
69. **CO2 Signal / Electricity Maps** (https://www.electricitymaps.com/) — Real-time carbon intensity and clean energy grid metrics.
70. **eBird API** (https://ebird.org/home) — Global bird sighting observations and biodiversity species location data.

### 4.8 Entertainment, Movies & Content
71. **TMDB (The Movie Database)** (https://developer.themoviedb.org/) — Extensive database of movies, TV shows, actors, ratings, and posters.
72. **Spotify Web API** (https://developer.spotify.com/) — Query music metadata, artist profiles, playlists, and playback state.
73. **OMDb API** (https://www.omdbapi.com/) — Open movie database REST service supporting IMDb ratings and movie info.
74. **RAWG Video Games API** (https://rawg.io/apidocs) — Massive database covering over 500,000 video games across multiple platforms.
75. **Jikan API** (https://jikan.moe/) — Unofficial MyAnimeList REST API providing anime and manga metadata.
76. **YouTube Data API** (https://developers.google.com/youtube/v3) — Search videos, manage playlists, and analyze channel metrics.
77. **Open Library API** (https://openlibrary.org/developers/api) — Internet Archive project indexing millions of book records and covers.
78. **PokeAPI** (https://pokeapi.co/) — Highly detailed RESTful API containing full Pokémon franchise statistics.
79. **Spoonacular Food API** (https://spoonacular.com/food-api) — Recipe search, ingredient parsing, and nutritional breakdown analysis.
80. **Comic Vine API** (https://comicvine.gamespot.com/api/) — Metadata on comic books, characters, creators, and publishers.

### 4.9 News, Search & Reference Data
81. **NewsAPI** (https://newsapi.org/) — Live headlines and historical article indexing from thousands of international news publications.
82. **Wikipedia API** (https://www.mediawiki.org/wiki/API:Main_page) — Fetch article excerpts, structured media, and full wiki page data.
83. **Hacker News Firebase API** (https://github.com/HackerNews/API) — Real-time access to top stories, comments, and user profiles.
84. **Tavily Search API** (https://tavily.com/) — AI-optimized web search query API for LLM retrieval and agent search.
85. **DuckDuckGo Instant Answer API** (https://duckduckgo.com/api) — Zero-click info boxes, topic disambiguation, and quick definitions.
86. **Crossref REST API** (https://www.crossref.org/documentation/retrieve-metadata/rest-api/) — Search metadata for millions of peer-reviewed academic papers.
87. **Gutendex** (https://gutendex.com/) — Web API for Project Gutenberg's library of public-domain books.
88. **Dictionary API** (https://dictionaryapi.dev/) — Free english dictionary definitions, phonetics, and audio pronunciations.
89. **Open Trivia DB** (https://opentdb.com/) — Category-driven trivia question database for game creation.
90. **Currents API** (https://currentsapi.services/) — Multi-language global news collection API.

### 4.10 Prototyping, Mocking & Utility
91. **JSONPlaceholder** (https://jsonplaceholder.typicode.com/) — Fake REST API for rapid frontend prototyping and CRUD operation testing.
92. **ReqRes** (https://reqres.in/) — Simulated REST API with pre-baked responses for testing client requests.
93. **Faker API** (https://fakerapi.it/) — Generate mock structured JSON payload items (addresses, products, companies).
94. **DummyJSON** (https://dummyjson.com/) — Free fake REST API providing mock ecommerce, user, and post datasets.
95. **URLHaus API** (https://urlhaus-api.abuse.ch/) — Malware URL database feed for threat intelligence testing.
96. **Have I Been Pwned API** (https://haveibeenpwned.com/API/v3) — Check if account credentials have appeared in public data breaches.
97. **Restful Booker** (https://restful-booker.herokuapp.com/) — Intentional hotel booking API crafted specifically for API automation testing.
98. **Swagger Petstore** (https://petstore.swagger.io/) — Benchmark OpenAPI spec sample API for testing REST clients.
99. **httpbin.org** (https://httpbin.org/) — HTTP request and response testing service returning exact headers and methods sent.
100. **Shields.io** (https://shields.io/) — Concise dynamic SVG status badge generation API for GitHub README files.

### 4.11 Security, Identity & Compliance
* **Have I Been Pwned API** — Check if passwords or account credentials have appeared in public data breaches.
* **VirusTotal API** — Analyze suspicious files, domains, IP addresses, and URLs for malware.
* **AbuseIPDB** — Check and report IP addresses engaging in abusive behavior or spam.
* **Shodan API** — Search engine for internet-connected devices, open ports, and vulnerabilities.
* **SSL Labs API** — Automated server test for SSL/TLS configuration analysis.
* **Authress** — B2B authorization-as-a-service platform for fine-grained access control.
* **Passbase API** — Identity verification and KYC infrastructure APIs.
* **Stytch API** — Developer-first passwordless authentication APIs and SDKs.
* **Fingerprint API** — High-accuracy device identification and fraud prevention API.
* **Secoda API** — Data discovery, lineage, and cataloguing automation platform.

### 4.12 Productivity, Documents & Office Tools
* **Notion API** — Programmatically read, write, and structure pages and databases in Notion.
* **Airtable API** — Turn spreadsheets into relational databases with full REST access.
* **Google Drive API** — Manage files, folders, permissions, and cloud storage programmatically.
* **Microsoft Graph API** — Unified endpoint for Office 365, Outlook, OneDrive, and SharePoint data.
* **Trello REST API** — Manage boards, lists, cards, and team workflows.
* **Asana API** — Task tracking, project management, and workspace automation.
* **Todoist API** — Manage tasks, projects, labels, and personal productivity streams.
* **PDFShift** — High-speed HTML-to-PDF generation REST API for invoices and reports.
* **DocRaptor** — Enterprise HTML-to-PDF and XML document generation service.
* **Cloudmersive Document Processing** — Convert, edit, and parse docx, pdf, xlsx, and image formats.

### 4.13 Social Media, Analytics & Community
* **Reddit API** — Access subreddit threads, user profiles, comments, and voting metrics.
* **Mastodon API** — Decentralized microblogging platform REST and streaming API.
* **Dev.to API** — Publish, fetch, and manage developer articles and community posts.
* **Hashnode API** — GraphQL API for blogging platforms and developer publishing workflows.
* **Medium API** — Publish and manage stories on Medium publications.
* **Substack API** — Newsletter subscription management and publication content retrieval.
* **Twitch API** — Live streaming metadata, user profiles, clips, and chat interactions.
* **Pinterest API** — Pin creation, board management, and visual discovery data.
* **Dribbble API** — Showcase and discover creative design shots, portfolios, and user activity.
* **Behance API** — Access creative project showcases, galleries, and designer profiles.

### 4.14 Audio, Music & Speech Processing
* **Last.fm API** — Music catalog metadata, artist tracks, user scrobbles, and recommendations.
* **Genius API** — Lyrics search, annotations, and artist song metadata.
* **Audible API** — Audiobook catalogs, narrator details, and listening metrics.
* **Speechmatics API** — Advanced speech recognition and multilingual transcription.
* **Rev.ai** — Speech-to-text transcription and subtitle generation APIs.
* **ElevenLabs API** — Ultra-realistic generative AI voice cloning and text-to-speech synthesis.
* **Play.ht API** — AI text-to-speech audio generation and podcast hosting tools.
* **Deepgram API** — Enterprise-grade speech-to-text, audio intelligence, and voice AI platform.
* **VocalRemover API** — AI-powered vocal isolation and music stem separation.
* **Radio-Browser API** — Open directory of streaming online radio stations worldwide.

### 4.15 Gaming, Anime & Pop Culture
* **IGDB API** — Internet Game Database providing extensive game metadata, covers, and release dates.
* **Giant Bomb API** — Video game encyclopedia data, characters, concepts, and reviews.
* **Steam Web API** — Query user inventories, game achievements, friend lists, and store data.
* **League of Legends Riot API** — Summoner profiles, match histories, and game telemetry data.
* **Fortnite API** — Cosmetic items, map details, and shop rotations.
* **Minecraft API** — Player UUID lookups, skin textures, and game session verification.
* **Chess.com API** — Player stats, leaderboards, live game archives, and puzzles.
* **Lichess API** — Open chess server API for games, analysis, puzzles, and bot integration.
* **Kitsu API** — Modern anime and manga database with rich filtering and categorization.
* **Anilist API** — GraphQL API for tracking anime, manga, and character stats.

### 4.16 IoT, Hardware & Smart Home
* **Home Assistant API** — Open-source home automation hub REST and WebSocket API.
* **SmartThings API** — Control smart home devices, sensors, and lighting systems.
* **Particle API** — IoT hardware cloud platform for connected microcontrollers.
* **Adafruit IO** — Simple IoT data visualization, storage, and device control broker.
* **Blynk API** — IoT firmware and mobile dashboard builder platform.
* **IFTTT API** — Trigger cross-service automations between smart hardware and web apps.
* **OpenHardwareIO** — Community database for open-source electronics and schematics.
* **ESPHome API** — Control ESP8266/ESP32 boards using simple configuration files.
* **Tuya IoT API** — Cloud developer platform for smart home appliances and hardware.
* **Wappalyzer API** — Identify technologies, CMS frameworks, and hardware used on websites.

### 4.17 Travel, Transport & Navigation
* **Skyscanner API** — Global flight pricing, schedules, and travel aggregation data.
* **Amadeus for Developers** — Airline flight booking, hotel search, and travel safety APIs.
* **Aviationstack** — Real-time flight tracking data, airline schedules, and airport codes.
* **OpenSky Network API** — Live air traffic control data and aircraft transponder signals.
* **Amtrak API** — Train schedules, station data, and rail transit routes.
* **BGBike / City Bikes API** — Real-time public bicycle sharing systems worldwide.
* **Transit Land API** — Unified open transit data for global bus, rail, and ferry networks.
* **Overpass API** — Custom read-only queries against OpenStreetMap spatial data.
* **Geoapify Routing API** — Multi-modal route planning for driving, walking, and cycling.
* **Parkopedia API** — Global parking space availability, pricing, and location details.

### 4.18 Reference, Genealogy & Fact-Checking
* **Wikidata API** — Structured knowledge base querying entities, facts, and relations.
* **DBpedia SPARQL Endpoint** — Query structured data extracted from Wikipedia editions.
* **Internet Archive API** — Access millions of archived web pages, books, audio, and video files.
* **Project Gutenberg API** — Public domain ebook metadata and raw text downloads.
* **FamilySearch API** — Global genealogical records, family trees, and historical archives.
* **Library of Congress API** — Digital collections, photographs, maps, and historical manuscripts.
* **OpenSanctions API** — Track global politically exposed persons, sanctions, and crime databases.
* **FactCheck.org API** — Political fact-checking claims and investigative reports.
* **Snopes API** — Urban legends, rumors, and misinformation verification database.
* **Every Politician API** — Data on politicians, parliaments, and legislative bodies worldwide.

### 4.19 Environment, Energy & Sustainability
* **Carbon Interface** — Carbon footprint estimation API for electricity, flights, and shipping.
* **Climatiq API** — Carbon emission calculation engine based on authoritative scientific models.
* **Global Forest Watch API** — Monitor deforestation, forest fires, and land cover changes.
* **World Bank Open Data API** — Global development indicators, poverty metrics, and economic data.
* **OECD Data API** — Economic statistics and social indicators across member nations.
* **FAOSTAT API** — Food and agriculture statistics, crop yields, and livestock data.
* **UN Comtrade API** — International trade statistics and commodity import/export records.
* **WaterAPI** — Real-time streamflow, water temperature, and hydrological metrics from USGS.
* **NOAA Climate Data Online API** — Historical weather observations and climate research datasets.
* **Open Energy Data Initiative (OEDI)** — U.S. Department of Energy open power systems and renewable energy.
