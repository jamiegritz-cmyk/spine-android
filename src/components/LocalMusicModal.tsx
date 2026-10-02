import React, { useRef, useState } from 'react';
import { parseAudioFile } from '../utils/id3Parser';
import { Release, Track } from '../types/music';
import { identifyAndFetchArtwork, extractDominantColorFromImage } from '../services/musicBrainzService';
import { cacheArtwork } from '../services/artworkCache';
import { X, FolderOpen, Music, Loader2, Sparkles, CheckCircle2 } from 'lucide-react';

interface LocalMusicModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAddReleases: (newReleases: Release[]) => void;
}

export const LocalMusicModal: React.FC<LocalMusicModalProps> = ({
  isOpen,
  onClose,
  onAddReleases
}) => {
  const [isScanning, setIsScanning] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');
  const [progressCount, setProgressCount] = useState({ current: 0, total: 0 });
  const fileInputRef = useRef<HTMLInputElement>(null);
  const folderInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  const handleFiles = async (files: FileList | null) => {
    if (!files || files.length === 0) return;

    setIsScanning(true);
    const audioFiles = Array.from(files).filter(
      (file) =>
        file.type.startsWith('audio/') ||
        /\.(mp3|flac|wav|m4a|aac|ogg|wma)$/i.test(file.name)
    );

    if (audioFiles.length === 0) {
      setStatusMessage('No supported audio files found (.mp3, .flac, .wav, .m4a).');
      setIsScanning(false);
      return;
    }

    setProgressCount({ current: 0, total: audioFiles.length });
    setStatusMessage(`Scanning ${audioFiles.length} file(s) for ID3 metadata...`);

    const albumsMap = new Map<
      string,
      {
        title: string;
        artist: string;
        year?: number;
        artworkUrl?: string;
        tracks: Track[];
      }
    >();

    // Step 1: Parse ID3 metadata and embedded APIC artwork
    for (let i = 0; i < audioFiles.length; i++) {
      const file = audioFiles[i];
      setProgressCount({ current: i + 1, total: audioFiles.length });
      setStatusMessage(`Reading tags [${i + 1}/${audioFiles.length}]: ${file.name}`);

      try {
        const parsed = await parseAudioFile(file);
        const albumKey = `${parsed.album.toLowerCase().trim()}_${parsed.track.artist.toLowerCase().trim()}`;

        if (!albumsMap.has(albumKey)) {
          albumsMap.set(albumKey, {
            title: parsed.album,
            artist: parsed.track.artist,
            year: parsed.year,
            artworkUrl: parsed.artworkBlobUrl,
            tracks: [parsed.track]
          });
        } else {
          const existing = albumsMap.get(albumKey)!;
          existing.tracks.push(parsed.track);
          if (!existing.artworkUrl && parsed.artworkBlobUrl) {
            existing.artworkUrl = parsed.artworkBlobUrl;
          }
          if (!existing.year && parsed.year) {
            existing.year = parsed.year;
          }
        }
      } catch (err) {
        console.warn('Error reading audio file:', file.name, err);
      }
    }

    // Step 2: For each release, use embedded artwork or query MusicBrainz / Cover Art Archive
    const createdReleases: Release[] = [];
    let releaseIdx = 0;

    for (const [_, data] of albumsMap.entries()) {
      let finalArtwork = data.artworkUrl;

      // If embedded artwork was missing, look up on MusicBrainz & Cover Art Archive
      if (!finalArtwork) {
        setStatusMessage(
          `Looking up genuine cover art for "${data.title}" on MusicBrainz & Cover Art Archive...`
        );
        try {
          const onlineArt = await identifyAndFetchArtwork(data.title, data.artist);
          if (onlineArt) {
            finalArtwork = onlineArt;
          }
        } catch (e) {
          console.warn('MusicBrainz lookup failed for:', data.title, e);
        }
      }

      // Default high-contrast physical CD jewel graphic if truly unindexed
      if (!finalArtwork) {
        finalArtwork =
          'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300" viewBox="0 0 300 300"><rect width="300" height="300" fill="%231a1816"/><circle cx="150" cy="150" r="110" fill="none" stroke="%233a3835" stroke-width="2"/><circle cx="150" cy="150" r="35" fill="%23101010" stroke="%23555555" stroke-width="2"/><text x="150" y="155" fill="%23888888" font-size="12" font-family="sans-serif" text-anchor="middle">PHYSICAL CD</text></svg>';
      }

      // Extract colors from the real artwork for authentic spine styling
      const colors = await extractDominantColorFromImage(finalArtwork);
      const catCode = `LOC-${1000 + releaseIdx}`;

      createdReleases.push({
        id: `local_rel_${Date.now()}_${releaseIdx}`,
        title: data.title,
        artist: data.artist,
        year: data.year || new Date().getFullYear(),
        artworkUrl: finalArtwork,
        spineColor: colors.bg,
        spineTextColor: colors.text,
        catalogNumber: catCode,
        genre: 'Local Music',
        tracks: data.tracks.sort((a, b) => a.trackNumber - b.trackNumber),
        isLocal: true,
        spineStyle: 'custom'
      });

      releaseIdx++;
    }

    setIsScanning(false);
    if (createdReleases.length > 0) {
      onAddReleases(createdReleases);
      onClose();
    } else {
      setStatusMessage('No valid music tracks found to import.');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="w-full max-w-md bg-[#161514] border border-neutral-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col">
        {/* Header */}
        <div className="px-5 py-4 border-b border-neutral-800 flex items-center justify-between bg-neutral-900/40">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-lg bg-neutral-800 text-neutral-200">
              <FolderOpen className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-neutral-100">
                Scan Local Device Music
              </h3>
              <p className="text-[11px] text-neutral-400">
                Read audio files, embedded artwork & MusicBrainz
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            disabled={isScanning}
            className="p-1 rounded text-neutral-400 hover:text-white hover:bg-neutral-800"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 flex flex-col gap-4">
          <div className="p-3 bg-neutral-950/60 rounded-xl border border-neutral-800/80 text-xs text-neutral-300 space-y-2 leading-relaxed">
            <p className="flex items-center gap-1.5 font-medium text-white">
              <Sparkles className="w-3.5 h-3.5 text-amber-400 shrink-0" />
              Real Artwork & Music Library Architecture:
            </p>
            <ol className="list-decimal list-inside space-y-1 text-neutral-400 text-[11px]">
              <li>Extracts embedded ID3 cover art from audio files.</li>
              <li>
                If artwork is missing, queries <strong>MusicBrainz</strong> and fetches genuine
                cover art from the <strong>Cover Art Archive</strong>.
              </li>
              <li>Caches the artwork locally so it stays available.</li>
              <li>Generates physical CD spines derived directly from the real artwork.</li>
            </ol>
          </div>

          {isScanning ? (
            <div className="p-6 bg-neutral-950/80 rounded-xl border border-neutral-800 flex flex-col items-center justify-center gap-3 text-center">
              <Loader2 className="w-7 h-7 text-neutral-200 animate-spin" />
              <p className="text-xs font-medium text-neutral-100">{statusMessage}</p>
              {progressCount.total > 0 && (
                <p className="text-[11px] font-mono text-neutral-400">
                  {progressCount.current} / {progressCount.total} files processed
                </p>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-3">
              {/* Select Individual Audio Files */}
              <button
                onClick={() => fileInputRef.current?.click()}
                className="p-4 rounded-xl border border-neutral-800 hover:border-neutral-700 bg-neutral-950/50 hover:bg-neutral-800/40 flex flex-col items-center justify-center text-center gap-2 group transition-all"
              >
                <Music className="w-6 h-6 text-neutral-400 group-hover:text-white transition-colors" />
                <span className="text-xs font-medium text-neutral-200">
                  Select Audio Files
                </span>
                <span className="text-[10px] text-neutral-500">.mp3, .flac, .wav, .m4a</span>
              </button>

              {/* Select Folder */}
              <button
                onClick={() => folderInputRef.current?.click()}
                className="p-4 rounded-xl border border-neutral-800 hover:border-neutral-700 bg-neutral-950/50 hover:bg-neutral-800/40 flex flex-col items-center justify-center text-center gap-2 group transition-all"
              >
                <FolderOpen className="w-6 h-6 text-neutral-400 group-hover:text-white transition-colors" />
                <span className="text-xs font-medium text-neutral-200">
                  Select Music Folder
                </span>
                <span className="text-[10px] text-neutral-500">Scan entire directory</span>
              </button>
            </div>
          )}

          {statusMessage && !isScanning && (
            <div className="p-3 bg-neutral-950/60 rounded border border-neutral-800 text-xs text-neutral-400">
              {statusMessage}
            </div>
          )}
        </div>

        {/* Hidden inputs */}
        <input
          ref={fileInputRef}
          type="file"
          accept="audio/*,.mp3,.flac,.wav,.m4a,.ogg"
          multiple
          className="hidden"
          onChange={(e) => handleFiles(e.target.files)}
        />
        <input
          ref={folderInputRef}
          type="file"
          // @ts-expect-error webkitdirectory is standard for folder pickers
          webkitdirectory=""
          directory=""
          multiple
          className="hidden"
          onChange={(e) => handleFiles(e.target.files)}
        />

        {/* Footer */}
        <div className="px-5 py-3 border-t border-neutral-800 bg-neutral-950/40 flex justify-between items-center text-xs">
          <span className="text-[11px] text-neutral-500 font-mono">
            Keeps demo releases until local music is scanned
          </span>
          <button
            onClick={onClose}
            disabled={isScanning}
            className="px-4 py-1.5 rounded-lg text-xs font-medium text-neutral-300 hover:text-white hover:bg-neutral-800 transition-colors"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};
