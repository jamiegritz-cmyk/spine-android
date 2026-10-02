import { Track, Release } from '../types/music';

/**
 * Parses client-side audio files (.mp3, .wav, .flac, .m4a, .ogg)
 * Extracts embedded ID3v2 tags and APIC cover artwork.
 * If cover artwork is missing, falls back to MusicBrainz / Cover Art Archive API.
 */

export interface ParsedAudioResult {
  track: Track;
  album: string;
  year?: number;
  artworkBlobUrl?: string;
}

export async function parseAudioFile(file: File): Promise<ParsedAudioResult> {
  const fileNameWithoutExt = file.name.replace(/\.[^/.]+$/, '');
  let title = fileNameWithoutExt;
  let artist = 'Unknown Artist';
  let album = 'Unknown Album';
  let year: number | undefined;
  let trackNum = 1;
  let artworkBlobUrl: string | undefined;

  // Read first 256KB to inspect ID3 header
  try {
    const buffer = await file.slice(0, 256 * 1024).arrayBuffer();
    const view = new DataView(buffer);

    // Check for ID3v2 tag identifier
    if (
      view.getUint8(0) === 0x49 && // 'I'
      view.getUint8(1) === 0x44 && // 'D'
      view.getUint8(2) === 0x33    // '3'
    ) {
      const version = view.getUint8(3);
      const tagSize =
        ((view.getUint8(6) & 0x7f) << 21) |
        ((view.getUint8(7) & 0x7f) << 14) |
        ((view.getUint8(8) & 0x7f) << 7) |
        (view.getUint8(9) & 0x7f);

      // Parse frames within available buffer
      let offset = 10;
      const maxOffset = Math.min(buffer.byteLength, tagSize + 10);

      while (offset + 10 < maxOffset) {
        let frameId = '';
        for (let i = 0; i < 4; i++) {
          frameId += String.fromCharCode(view.getUint8(offset + i));
        }

        if (!/^[A-Z0-9]{4}$/.test(frameId)) {
          break;
        }

        let frameSize = 0;
        if (version >= 4) {
          // Syncsafe integer in ID3v2.4
          frameSize =
            ((view.getUint8(offset + 4) & 0x7f) << 21) |
            ((view.getUint8(offset + 5) & 0x7f) << 14) |
            ((view.getUint8(offset + 6) & 0x7f) << 7) |
            (view.getUint8(offset + 7) & 0x7f);
        } else {
          frameSize = view.getUint32(offset + 4);
        }

        if (frameSize <= 0 || offset + 10 + frameSize > buffer.byteLength) {
          break;
        }

        const frameDataOffset = offset + 10;
        const frameBytes = new Uint8Array(buffer, frameDataOffset, frameSize);

        if (frameId === 'TIT2') {
          title = decodeId3Text(frameBytes) || title;
        } else if (frameId === 'TPE1') {
          artist = decodeId3Text(frameBytes) || artist;
        } else if (frameId === 'TALB') {
          album = decodeId3Text(frameBytes) || album;
        } else if (frameId === 'TYER' || frameId === 'TDRC') {
          const rawYear = decodeId3Text(frameBytes);
          const parsedYear = parseInt(rawYear?.substring(0, 4) || '', 10);
          if (!isNaN(parsedYear)) year = parsedYear;
        } else if (frameId === 'TRCK') {
          const rawTrk = decodeId3Text(frameBytes);
          const parsed = parseInt(rawTrk?.split('/')[0] || '1', 10);
          if (!isNaN(parsed)) trackNum = parsed;
        } else if (frameId === 'APIC') {
          // Attached picture frame
          try {
            const picBlob = extractApicBlob(frameBytes);
            if (picBlob) {
              artworkBlobUrl = URL.createObjectURL(picBlob);
            }
          } catch {
            // Fallback gracefully
          }
        }

        offset += 10 + frameSize;
      }
    }
  } catch (err) {
    console.warn('ID3 parsing skipped or failed:', err);
  }

  // Get duration using temporary HTML5 Audio
  const duration = await getAudioDuration(file);
  const audioBlobUrl = URL.createObjectURL(file);

  return {
    track: {
      id: `local_track_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
      title,
      artist,
      duration: duration || 210,
      audioUrl: audioBlobUrl,
      trackNumber: trackNum
    },
    album: album || 'Local Audio File',
    year,
    artworkBlobUrl
  };
}

function decodeId3Text(bytes: Uint8Array): string {
  if (bytes.length <= 1) return '';
  const encoding = bytes[0];
  const textBytes = bytes.slice(1);

  if (encoding === 0) {
    // ISO-8859-1
    return new TextDecoder('iso-8859-1').decode(textBytes).replace(/\0/g, '').trim();
  } else if (encoding === 1 || encoding === 2) {
    // UTF-16
    return new TextDecoder('utf-16').decode(textBytes).replace(/\0/g, '').trim();
  } else if (encoding === 3) {
    // UTF-8
    return new TextDecoder('utf-8').decode(textBytes).replace(/\0/g, '').trim();
  }
  return new TextDecoder('utf-8').decode(textBytes).replace(/\0/g, '').trim();
}

function extractApicBlob(bytes: Uint8Array): Blob | null {
  if (bytes.length < 10) return null;
  const encoding = bytes[0];
  let offset = 1;

  // Read MIME type (null terminated ASCII)
  let mime = '';
  while (offset < bytes.length && bytes[offset] !== 0) {
    mime += String.fromCharCode(bytes[offset]);
    offset++;
  }
  offset++; // skip null terminator

  if (!mime || mime === 'image/') mime = 'image/jpeg';

  // Picture type (1 byte)
  offset++;

  // Skip description (null terminated according to encoding)
  if (encoding === 1 || encoding === 2) {
    // UTF-16 null terminator is 2 bytes
    while (offset + 1 < bytes.length && !(bytes[offset] === 0 && bytes[offset + 1] === 0)) {
      offset += 2;
    }
    offset += 2;
  } else {
    while (offset < bytes.length && bytes[offset] !== 0) {
      offset++;
    }
    offset++;
  }

  if (offset >= bytes.length) return null;

  const imageBuffer = bytes.slice(offset);
  return new Blob([imageBuffer], { type: mime });
}

function getAudioDuration(file: File): Promise<number> {
  return new Promise((resolve) => {
    const audio = new Audio();
    const url = URL.createObjectURL(file);
    audio.src = url;
    audio.addEventListener('loadedmetadata', () => {
      URL.revokeObjectURL(url);
      resolve(Math.round(audio.duration));
    });
    audio.addEventListener('error', () => {
      URL.revokeObjectURL(url);
      resolve(180);
    });
  });
}

/**
 * Searches MusicBrainz & Cover Art Archive if album artwork was not embedded
 */
export async function fetchCoverArtArchiveArtwork(album: string, artist: string): Promise<string | null> {
  try {
    const cleanAlbum = encodeURIComponent(album.trim());
    const cleanArtist = encodeURIComponent(artist.trim());
    const mbUrl = `https://musicbrainz.org/ws/2/release/?query=release:${cleanAlbum}%20AND%20artist:${cleanArtist}&fmt=json&limit=1`;

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 4000);

    const res = await fetch(mbUrl, {
      signal: controller.signal,
      headers: { 'User-Agent': 'SpineMusicPlayer/1.0.0 (jamiegritz@gmail.com)' }
    });
    clearTimeout(timeout);

    if (!res.ok) return null;
    const data = await res.json();
    const release = data.releases?.[0];
    if (!release?.id) return null;

    // Check Cover Art Archive for front cover
    const caaUrl = `https://coverartarchive.org/release/${release.id}/front-500`;
    const imgCheck = await fetch(caaUrl, { method: 'HEAD' });
    if (imgCheck.ok) {
      return caaUrl;
    }
    return null;
  } catch {
    return null;
  }
}
