export interface Track {
  id: string;
  title: string;
  artist: string;
  duration: number; // in seconds
  audioUrl?: string; // object URL or synthesized
  trackNumber: number;
  syntheticSeed?: number; // for procedural audio playback
}

export type SpineStyleType =
  | 'oasis_definitely_maybe'
  | 'radiohead_ok_computer'
  | 'beatles_abbey_road'
  | 'pink_floyd_dark_side'
  | 'blur_parklife'
  | 'the_verve_urban_hymns'
  | 'coldplay_parachutes'
  | 'kasabian'
  | 'the_stone_roses'
  | 'the_killers_hot_fuss'
  | 'queen_greatest_hits'
  | 'david_bowie_hunky_dory'
  | 'pulp_different_class'
  | 'arctic_monkeys_am'
  | 'muse_origin_of_symmetry'
  | 'the_vaccines'
  | 'oasis_morning_glory'
  | 'the_charlatans'
  | 'rem_automatic'
  | 'default'
  | 'custom';

export interface Release {
  id: string;
  title: string;
  artist: string;
  year: number;
  artworkUrl: string;
  spineColor: string;
  spineTextColor?: string;
  catalogNumber: string;
  genre: string;
  tracks: Track[];
  isLocal?: boolean;
  spineStyle?: SpineStyleType;
  spineArtworkSliceUrl?: string;
}

export type RepeatMode = 'off' | 'all' | 'one';

export type DeviceOrientation = 'portrait' | 'landscape' | 'tablet';
