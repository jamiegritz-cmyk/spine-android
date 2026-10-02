import React, { useState } from 'react';
import { Release } from '../types/music';
import {
  searchMusicBrainzCandidates,
  MusicBrainzCandidate,
  extractDominantColorFromImage
} from '../services/musicBrainzService';
import { cacheArtwork } from '../services/artworkCache';
import { X, Search, Check, Image as ImageIcon, Loader2, Sparkles, Upload } from 'lucide-react';

interface CorrectArtworkModalProps {
  isOpen: boolean;
  onClose: () => void;
  release: Release;
  onUpdateArtwork: (updatedRelease: Release) => void;
}

export const CorrectArtworkModal: React.FC<CorrectArtworkModalProps> = ({
  isOpen,
  onClose,
  release,
  onUpdateArtwork
}) => {
  const [searchQuery, setSearchQuery] = useState(`${release.title} ${release.artist}`);
  const [isSearching, setIsSearching] = useState(false);
  const [candidates, setCandidates] = useState<MusicBrainzCandidate[]>([]);
  const [selectedMbid, setSelectedMbid] = useState<string | null>(null);
  const [customImageUrl, setCustomImageUrl] = useState('');
  const [isApplying, setIsApplying] = useState(false);

  if (!isOpen) return null;

  const handleSearch = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!searchQuery.trim()) return;

    setIsSearching(true);
    const results = await searchMusicBrainzCandidates(release.title, release.artist);
    setCandidates(results);
    setIsSearching(false);
  };

  const handleSelectCandidate = async (candidate: MusicBrainzCandidate) => {
    setSelectedMbid(candidate.id);
    setIsApplying(true);

    const newArtUrl = candidate.frontCoverUrl || release.artworkUrl;
    const colors = await extractDominantColorFromImage(newArtUrl);

    // Cache locally
    const cacheKey = `mb_caa_${release.title.toLowerCase()}_${release.artist.toLowerCase()}`;
    await cacheArtwork(cacheKey, newArtUrl);

    const updated: Release = {
      ...release,
      title: candidate.title || release.title,
      artist: candidate.artist || release.artist,
      year: candidate.year || release.year,
      artworkUrl: newArtUrl,
      spineColor: colors.bg,
      spineTextColor: colors.text
    };

    onUpdateArtwork(updated);
    setIsApplying(false);
    onClose();
  };

  const handleApplyCustomUrl = async () => {
    if (!customImageUrl.trim()) return;
    setIsApplying(true);
    const colors = await extractDominantColorFromImage(customImageUrl);

    const updated: Release = {
      ...release,
      artworkUrl: customImageUrl,
      spineColor: colors.bg,
      spineTextColor: colors.text
    };

    onUpdateArtwork(updated);
    setIsApplying(false);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="w-full max-w-lg bg-[#161514] border border-neutral-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="px-5 py-3.5 border-b border-neutral-800 flex items-center justify-between bg-neutral-900/60">
          <div className="flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-amber-400" />
            <div>
              <h3 className="text-sm font-semibold text-neutral-100">
                Identify & Correct Album Artwork
              </h3>
              <p className="text-[11px] text-neutral-400">
                MusicBrainz & Cover Art Archive Resolver
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded text-neutral-400 hover:text-white hover:bg-neutral-800"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Current Info */}
        <div className="p-4 bg-neutral-950/60 border-b border-neutral-800 flex items-center gap-3">
          <img
            src={release.artworkUrl}
            alt={release.title}
            className="w-14 h-14 rounded object-cover border border-white/10 shrink-0"
          />
          <div className="min-w-0 flex-1">
            <span className="text-[10px] font-mono text-neutral-500 uppercase">
              CURRENT RELEASE
            </span>
            <h4 className="text-sm font-semibold text-white truncate">{release.title}</h4>
            <p className="text-xs text-neutral-400 truncate">
              {release.artist} · {release.year}
            </p>
          </div>
        </div>

        {/* Search Input */}
        <div className="p-4 border-b border-neutral-800">
          <form onSubmit={handleSearch} className="flex gap-2">
            <div className="relative flex-1 flex items-center">
              <Search className="w-3.5 h-3.5 text-neutral-500 absolute left-3 pointer-events-none" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search MusicBrainz by album and artist..."
                className="w-full bg-neutral-900 text-xs rounded-lg pl-8 pr-3 py-2 border border-neutral-700/80 focus:border-neutral-500 focus:outline-none text-white"
              />
            </div>
            <button
              type="submit"
              disabled={isSearching}
              className="px-3.5 py-2 rounded-lg bg-neutral-100 hover:bg-white text-neutral-950 text-xs font-semibold shrink-0 transition-colors disabled:opacity-50"
            >
              {isSearching ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : 'Search'}
            </button>
          </form>
        </div>

        {/* Candidates List from MusicBrainz */}
        <div className="flex-1 overflow-y-auto p-4 space-y-2">
          <p className="text-[11px] font-mono uppercase tracking-wider text-neutral-500">
            MusicBrainz Matches ({candidates.length})
          </p>

          {isSearching ? (
            <div className="py-10 flex flex-col items-center justify-center gap-2 text-neutral-400 text-xs">
              <Loader2 className="w-6 h-6 animate-spin text-neutral-300" />
              <span>Querying MusicBrainz database...</span>
            </div>
          ) : candidates.length > 0 ? (
            candidates.map((cand) => (
              <div
                key={cand.id}
                onClick={() => handleSelectCandidate(cand)}
                className="p-3 rounded-xl border border-neutral-800 hover:border-neutral-700 bg-neutral-900/50 hover:bg-neutral-800/60 cursor-pointer transition-all flex items-center justify-between group"
              >
                <div className="flex items-center gap-3 min-w-0">
                  <div className="w-12 h-12 rounded bg-neutral-950 border border-neutral-800 overflow-hidden shrink-0 flex items-center justify-center">
                    <img
                      src={cand.frontCoverUrl}
                      alt={cand.title}
                      className="w-full h-full object-cover"
                      onError={(e) => {
                        // If Cover Art Archive doesn't have image for this specific MBID
                        (e.target as HTMLImageElement).src = release.artworkUrl;
                      }}
                    />
                  </div>
                  <div className="min-w-0">
                    <h5 className="text-xs font-semibold text-neutral-200 group-hover:text-white truncate">
                      {cand.title}
                    </h5>
                    <p className="text-[11px] text-neutral-400 truncate">{cand.artist}</p>
                    <p className="text-[10px] font-mono text-neutral-500">
                      {cand.year || 'Unknown year'} {cand.country ? `· ${cand.country}` : ''}{' '}
                      {cand.trackCount ? `· ${cand.trackCount} tracks` : ''}
                    </p>
                  </div>
                </div>

                <button className="px-2.5 py-1 rounded bg-neutral-800 group-hover:bg-white group-hover:text-neutral-950 text-neutral-300 text-xs font-medium shrink-0 transition-colors">
                  Apply
                </button>
              </div>
            ))
          ) : (
            <div className="py-8 text-center text-xs text-neutral-400">
              <p>No candidates found yet.</p>
              <p className="text-[11px] text-neutral-500 mt-1">
                Click "Search" above to search MusicBrainz for matching releases.
              </p>
            </div>
          )}
        </div>

        {/* Or Paste Direct Image URL */}
        <div className="p-4 border-t border-neutral-800 bg-neutral-950/40 flex items-center gap-2">
          <input
            type="text"
            value={customImageUrl}
            onChange={(e) => setCustomImageUrl(e.target.value)}
            placeholder="Or paste direct image URL (https://...)"
            className="flex-1 bg-neutral-900 text-xs rounded-lg px-3 py-1.5 border border-neutral-700/80 focus:border-neutral-500 focus:outline-none text-white"
          />
          <button
            onClick={handleApplyCustomUrl}
            disabled={!customImageUrl.trim() || isApplying}
            className="px-3 py-1.5 rounded-lg bg-neutral-800 hover:bg-neutral-700 text-neutral-200 text-xs font-medium disabled:opacity-40"
          >
            Apply URL
          </button>
        </div>
      </div>
    </div>
  );
};
