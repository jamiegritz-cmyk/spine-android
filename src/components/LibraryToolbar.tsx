import React, { useState } from 'react';
import { Search, FolderOpen, ArrowUpDown, X, Music } from 'lucide-react';

export type FilterCategory = 'all' | 'albums' | 'singles';
export type SortOption = 'title' | 'artist' | 'year' | 'recent';

interface LibraryToolbarProps {
  filter: FilterCategory;
  onFilterChange: (cat: FilterCategory) => void;
  searchQuery: string;
  onSearchChange: (q: string) => void;
  sortOption: SortOption;
  onSortChange: (opt: SortOption) => void;
  onOpenLocalPicker: () => void;
  totalReleases: number;
}

export const LibraryToolbar: React.FC<LibraryToolbarProps> = ({
  filter,
  onFilterChange,
  searchQuery,
  onSearchChange,
  sortOption,
  onSortChange,
  onOpenLocalPicker,
  totalReleases
}) => {
  const [showSearch, setShowSearch] = useState(false);

  return (
    <div className="w-full px-4 py-2 border-b border-neutral-800/80 bg-neutral-900/60 backdrop-blur-sm flex flex-col gap-2 z-20">
      {/* Top row: Brand / Stats / Action Icons */}
      <div className="flex items-center justify-between gap-3">
        {/* Brand Kicker with unboxed release count */}
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold tracking-wider text-neutral-300">SPINE</span>
          <span className="text-[11px] text-neutral-500 font-mono">
            {totalReleases} {totalReleases === 1 ? 'release' : 'releases'}
          </span>
        </div>

        {/* Right Action Icons: Add Local Music, Search Toggle, Sort Dropdown */}
        <div className="flex items-center gap-1.5">
          <button
            onClick={onOpenLocalPicker}
            className="flex items-center gap-1 px-2.5 py-1 rounded bg-neutral-800 hover:bg-neutral-700 text-neutral-200 text-xs font-medium transition-colors border border-white/5 active:scale-95"
            title="Scan local music files or folders on device"
          >
            <FolderOpen className="w-3.5 h-3.5 text-neutral-400" />
            <span className="hidden sm:inline">Add Music</span>
          </button>

          <button
            onClick={() => {
              setShowSearch(!showSearch);
              if (showSearch) onSearchChange('');
            }}
            className={`p-1.5 rounded transition-colors ${
              showSearch || searchQuery ? 'bg-neutral-800 text-white' : 'text-neutral-400 hover:text-neutral-200'
            }`}
            title="Search releases and songs"
          >
            <Search className="w-3.5 h-3.5" />
          </button>

          {/* Sort Selector */}
          <div className="relative flex items-center">
            <select
              value={sortOption}
              onChange={(e) => onSortChange(e.target.value as SortOption)}
              className="appearance-none bg-neutral-800 hover:bg-neutral-700/80 text-neutral-300 text-xs font-medium py-1 pl-2 pr-6 rounded border border-white/5 cursor-pointer focus:outline-none"
            >
              <option value="recent">Recent</option>
              <option value="title">Title (A-Z)</option>
              <option value="artist">Artist</option>
              <option value="year">Year</option>
            </select>
            <ArrowUpDown className="w-3 h-3 text-neutral-400 absolute right-1.5 pointer-events-none" />
          </div>
        </div>
      </div>

      {/* Expandable Search Input */}
      {showSearch && (
        <div className="relative w-full flex items-center">
          <Search className="w-3.5 h-3.5 text-neutral-500 absolute left-2.5 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search songs, albums, artists..."
            autoFocus
            className="w-full bg-neutral-950 text-neutral-100 text-xs rounded pl-8 pr-7 py-1.5 border border-neutral-700/80 focus:border-neutral-500 focus:outline-none"
          />
          {searchQuery && (
            <button
              onClick={() => onSearchChange('')}
              className="absolute right-2 text-neutral-500 hover:text-neutral-300"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>
      )}

      {/* Segmented Filter Control: All / Albums / Singles */}
      <div className="flex items-center gap-1 p-0.5 bg-neutral-950/80 rounded border border-neutral-800/80 w-fit">
        <button
          onClick={() => onFilterChange('all')}
          className={`px-3 py-1 text-xs font-medium rounded transition-colors ${
            filter === 'all'
              ? 'bg-neutral-800 text-neutral-100 shadow-sm'
              : 'text-neutral-400 hover:text-neutral-200'
          }`}
        >
          All
        </button>
        <button
          onClick={() => onFilterChange('albums')}
          className={`px-3 py-1 text-xs font-medium rounded transition-colors ${
            filter === 'albums'
              ? 'bg-neutral-800 text-neutral-100 shadow-sm'
              : 'text-neutral-400 hover:text-neutral-200'
          }`}
        >
          Albums
        </button>
        <button
          onClick={() => onFilterChange('singles')}
          className={`px-3 py-1 text-xs font-medium rounded transition-colors ${
            filter === 'singles'
              ? 'bg-neutral-800 text-neutral-100 shadow-sm'
              : 'text-neutral-400 hover:text-neutral-200'
          }`}
        >
          Singles & EPs
        </button>
      </div>
    </div>
  );
};
