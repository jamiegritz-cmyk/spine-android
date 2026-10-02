import { cacheArtwork, getCachedArtwork } from './artworkCache';

export interface MusicBrainzCandidate {
  id: string; // MBID
  title: string;
  artist: string;
  year?: number;
  country?: string;
  trackCount?: number;
  frontCoverUrl?: string;
}

/**
 * Searches MusicBrainz for release candidates matching an album title and artist.
 */
export async function searchMusicBrainzCandidates(
  album: string,
  artist: string
): Promise<MusicBrainzCandidate[]> {
  const cleanAlbum = album.trim().replace(/['"]/g, '');
  const cleanArtist = artist.trim().replace(/['"]/g, '');
  const query = `release:"${cleanAlbum}" AND artist:"${cleanArtist}"`;

  try {
    const url = `https://musicbrainz.org/ws/2/release/?query=${encodeURIComponent(
      query
    )}&fmt=json&limit=5`;

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 6000);

    const res = await fetch(url, {
      signal: controller.signal,
      headers: {
        'User-Agent': 'SpineMusicPlayer/1.0.0 (https://spinemusic.app)'
      }
    });
    clearTimeout(timeout);

    if (!res.ok) return [];
    const data = await res.json();
    const releases = data.releases || [];

    const candidates: MusicBrainzCandidate[] = [];

    for (const rel of releases) {
      const year = rel.date ? parseInt(rel.date.substring(0, 4), 10) : undefined;
      const artistCredit =
        rel['artist-credit']?.map((c: { name: string }) => c.name).join(' ') ||
        artist;

      const mbid = rel.id;
      const coverUrl = `https://coverartarchive.org/release/${mbid}/front-500`;

      candidates.push({
        id: mbid,
        title: rel.title,
        artist: artistCredit,
        year: isNaN(year!) ? undefined : year,
        country: rel.country,
        trackCount: rel['track-count'],
        frontCoverUrl: coverUrl
      });
    }

    return candidates;
  } catch (err) {
    console.warn('MusicBrainz search error:', err);
    return [];
  }
}

/**
 * Automatically identifies a release and fetches genuine Cover Art Archive artwork.
 * Checks local cache first before calling network.
 */
export async function identifyAndFetchArtwork(
  album: string,
  artist: string
): Promise<string | null> {
  const cacheKey = `mb_caa_${album.toLowerCase()}_${artist.toLowerCase()}`;
  const cached = await getCachedArtwork(cacheKey);
  if (cached) return cached;

  const candidates = await searchMusicBrainzCandidates(album, artist);
  if (candidates.length === 0) return null;

  // Check top candidate with Cover Art Archive HEAD request
  for (const candidate of candidates) {
    try {
      const checkRes = await fetch(candidate.frontCoverUrl!, { method: 'HEAD' });
      if (checkRes.ok || checkRes.status === 302 || checkRes.status === 307) {
        await cacheArtwork(cacheKey, candidate.frontCoverUrl!);
        return candidate.frontCoverUrl!;
      }
    } catch {
      // Continue to next candidate
    }
  }

  return null;
}

/**
 * Extracts dominant color from image URL using canvas to style the physical CD spine
 */
export function extractDominantColorFromImage(
  imageUrl: string
): Promise<{ bg: string; text: string }> {
  return new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'Anonymous';
    img.src = imageUrl;

    img.onload = () => {
      try {
        const canvas = document.createElement('canvas');
        canvas.width = 16;
        canvas.height = 16;
        const ctx = canvas.getContext('2d');
        if (!ctx) {
          resolve({ bg: '#1c1917', text: '#f5f5f4' });
          return;
        }
        ctx.drawImage(img, 0, 0, 16, 16);
        const data = ctx.getImageData(0, 0, 16, 16).data;

        let r = 0, g = 0, b = 0;
        const total = data.length / 4;
        for (let i = 0; i < data.length; i += 4) {
          r += data[i];
          g += data[i + 1];
          b += data[i + 2];
        }

        r = Math.round(r / total);
        g = Math.round(g / total);
        b = Math.round(b / total);

        // Darken slightly for physical spine paper depth
        r = Math.max(15, Math.round(r * 0.75));
        g = Math.max(15, Math.round(g * 0.75));
        b = Math.max(15, Math.round(b * 0.75));

        const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
        const textColor = luminance > 0.5 ? '#0f172a' : '#f8fafc';
        const hex = `#${((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1)}`;

        resolve({ bg: hex, text: textColor });
      } catch {
        resolve({ bg: '#1c1917', text: '#f5f5f4' });
      }
    };

    img.onerror = () => {
      resolve({ bg: '#1c1917', text: '#f5f5f4' });
    };
  });
}
