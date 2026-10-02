import { Release } from '../types/music';

// Generated image assets
import definitelyMaybeArt from '../assets/images/britpop_room_cover_1790947777019.jpg';
import okComputerArt from '../assets/images/album_radiohead_ok_computer_1790947700845.jpg';
import abbeyRoadArt from '../assets/images/album_beatles_abbey_road_1790947712111.jpg';
import darkSideArt from '../assets/images/album_pink_floyd_dark_side_1790947724945.jpg';
import velvetTidesArt from '../assets/images/album_velvet_tides_1790946355390.jpg';
import solarDriftArt from '../assets/images/album_solar_drift_1790946330596.jpg';
import midnightBlueArt from '../assets/images/album_midnight_blue_1790946319024.jpg';
import concreteEchoesArt from '../assets/images/album_concrete_echoes_1790946342911.jpg';

export const DEFAULT_CD_COLLECTION: Release[] = [
  {
    id: 'rks_audiotree_live',
    title: 'Rainbow Kitten Surprise on Audiotree Live [Explicit]',
    artist: 'Rainbow Kitten Surprise',
    year: 2017,
    artworkUrl: 'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80',
    spineColor: '#1c1a17',
    spineTextColor: '#f5f5f4',
    catalogNumber: 'RA-6405',
    genre: 'Indie Rock / Folk',
    spineStyle: 'default',
    tracks: [
      { id: 'rks_1', title: 'Counting Cards', artist: 'Rainbow Kitten Surprise', duration: 191, trackNumber: 1, syntheticSeed: 0 },
      { id: 'rks_2', title: 'Cocaine Jesus', artist: 'Rainbow Kitten Surprise', duration: 231, trackNumber: 2, syntheticSeed: 1 },
      { id: 'rks_3', title: 'Lady Lie', artist: 'Rainbow Kitten Surprise', duration: 198, trackNumber: 3, syntheticSeed: 2 },
      { id: 'rks_4', title: 'First Class', artist: 'Rainbow Kitten Surprise', duration: 204, trackNumber: 4, syntheticSeed: 3 }
    ]
  },
  {
    id: 'qotsa_songs_for_the_deaf',
    title: 'Songs for the Deaf',
    artist: 'Queens of the Stone Age',
    year: 2002,
    artworkUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80',
    spineColor: '#9e1b4d',
    spineTextColor: '#ffffff',
    catalogNumber: 'QS-8821',
    genre: 'Hard Rock',
    spineStyle: 'default',
    tracks: [
      { id: 'qotsa_1', title: 'No One Knows', artist: 'Queens of the Stone Age', duration: 255, trackNumber: 1, syntheticSeed: 1 },
      { id: 'qotsa_2', title: 'Go with the Flow', artist: 'Queens of the Stone Age', duration: 187, trackNumber: 2, syntheticSeed: 2 }
    ]
  },
  {
    id: 'portishead_dummy',
    title: 'Dummy',
    artist: 'Portishead',
    year: 1994,
    artworkUrl: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80',
    spineColor: '#8b1e2d',
    spineTextColor: '#ffffff',
    catalogNumber: 'PH-9411',
    genre: 'Trip Hop',
    spineStyle: 'default',
    tracks: [
      { id: 'ph_1', title: 'Mysterons', artist: 'Portishead', duration: 302, trackNumber: 1, syntheticSeed: 3 },
      { id: 'ph_2', title: 'Glory Box', artist: 'Portishead', duration: 308, trackNumber: 2, syntheticSeed: 0 }
    ]
  },
  {
    id: 'blur_parklife',
    title: 'Parklife',
    artist: 'Blur',
    year: 1994,
    artworkUrl: solarDriftArt,
    spineColor: '#eab308', // mustard yellow
    spineTextColor: '#1c1917',
    catalogNumber: 'FOODCD 10',
    genre: 'Britpop / Rock',
    spineStyle: 'blur_parklife',
    tracks: [
      { id: 'bp_1', title: 'Girls & Boys', artist: 'Blur', duration: 290, trackNumber: 1, syntheticSeed: 0 },
      { id: 'bp_2', title: 'Tracy Jacks', artist: 'Blur', duration: 260, trackNumber: 2, syntheticSeed: 1 },
      { id: 'bp_3', title: 'End of a Century', artist: 'Blur', duration: 166, trackNumber: 3, syntheticSeed: 2 },
      { id: 'bp_4', title: 'Parklife', artist: 'Blur', duration: 185, trackNumber: 4, syntheticSeed: 3 },
      { id: 'bp_5', title: 'Badhead', artist: 'Blur', duration: 205, trackNumber: 5, syntheticSeed: 0 },
      { id: 'bp_6', title: 'To the End', artist: 'Blur', duration: 240, trackNumber: 6, syntheticSeed: 1 }
    ]
  },
  {
    id: 'verve_urban_hymns',
    title: 'Urban Hymns',
    artist: 'The Verve',
    year: 1997,
    artworkUrl: velvetTidesArt,
    spineColor: '#1e2920', // deep olive
    spineTextColor: '#e2e8f0',
    catalogNumber: 'HUTCD 45',
    genre: 'Alternative Rock',
    spineStyle: 'the_verve_urban_hymns',
    tracks: [
      { id: 'vh_1', title: 'Bitter Sweet Symphony', artist: 'The Verve', duration: 358, trackNumber: 1, syntheticSeed: 1 },
      { id: 'vh_2', title: 'Sonnet', artist: 'The Verve', duration: 261, trackNumber: 2, syntheticSeed: 2 },
      { id: 'vh_3', title: 'The Drugs Don\'t Work', artist: 'The Verve', duration: 305, trackNumber: 3, syntheticSeed: 3 },
      { id: 'vh_4', title: 'Lucky Man', artist: 'The Verve', duration: 293, trackNumber: 4, syntheticSeed: 0 }
    ]
  },
  {
    id: 'radiohead_ok_computer',
    title: 'OK Computer',
    artist: 'Radiohead',
    year: 1997,
    artworkUrl: okComputerArt,
    spineColor: '#f1f5f9', // crisp white
    spineTextColor: '#0f172a',
    catalogNumber: 'CDNODATA 02',
    genre: 'Art Rock',
    spineStyle: 'radiohead_ok_computer',
    tracks: [
      { id: 'rc_1', title: 'Airbag', artist: 'Radiohead', duration: 284, trackNumber: 1, syntheticSeed: 2 },
      { id: 'rc_2', title: 'Paranoid Android', artist: 'Radiohead', duration: 383, trackNumber: 2, syntheticSeed: 3 },
      { id: 'rc_3', title: 'Subterranean Homesick Alien', artist: 'Radiohead', duration: 267, trackNumber: 3, syntheticSeed: 0 },
      { id: 'rc_4', title: 'Exit Music (For a Film)', artist: 'Radiohead', duration: 264, trackNumber: 4, syntheticSeed: 1 },
      { id: 'rc_5', title: 'Karma Police', artist: 'Radiohead', duration: 261, trackNumber: 5, syntheticSeed: 2 },
      { id: 'rc_6', title: 'No Surprises', artist: 'Radiohead', duration: 228, trackNumber: 6, syntheticSeed: 3 }
    ]
  },
  {
    id: 'oasis_definitely_maybe',
    title: 'Definitely Maybe',
    artist: 'Oasis',
    year: 1994,
    artworkUrl: definitelyMaybeArt,
    spineColor: '#f5edd6', // authentic cream
    spineTextColor: '#1c1917',
    catalogNumber: 'CRECD 169',
    genre: 'Britpop / Rock',
    spineStyle: 'oasis_definitely_maybe',
    tracks: [
      { id: 'odm_1', title: 'Rock \'n\' Roll Star', artist: 'Oasis', duration: 323, trackNumber: 1, syntheticSeed: 0 },
      { id: 'odm_2', title: 'Shakermaker', artist: 'Oasis', duration: 308, trackNumber: 2, syntheticSeed: 1 },
      { id: 'odm_3', title: 'Live Forever', artist: 'Oasis', duration: 276, trackNumber: 3, syntheticSeed: 2 },
      { id: 'odm_4', title: 'Up in the Sky', artist: 'Oasis', duration: 268, trackNumber: 4, syntheticSeed: 3 },
      { id: 'odm_5', title: 'Columbia', artist: 'Oasis', duration: 377, trackNumber: 5, syntheticSeed: 0 },
      { id: 'odm_6', title: 'Supersonic', artist: 'Oasis', duration: 283, trackNumber: 6, syntheticSeed: 1 },
      { id: 'odm_7', title: 'Slide Away', artist: 'Oasis', duration: 392, trackNumber: 7, syntheticSeed: 2 }
    ]
  },
  {
    id: 'beatles_abbey_road',
    title: 'Abbey Road',
    artist: 'The Beatles',
    year: 1969,
    artworkUrl: abbeyRoadArt,
    spineColor: '#121212', // deep black
    spineTextColor: '#f8fafc',
    catalogNumber: 'CDP 7 46446 2',
    genre: 'Rock',
    spineStyle: 'beatles_abbey_road',
    tracks: [
      { id: 'bar_1', title: 'Come Together', artist: 'The Beatles', duration: 259, trackNumber: 1, syntheticSeed: 1 },
      { id: 'bar_2', title: 'Something', artist: 'The Beatles', duration: 182, trackNumber: 2, syntheticSeed: 2 },
      { id: 'bar_3', title: 'Here Comes the Sun', artist: 'The Beatles', duration: 185, trackNumber: 3, syntheticSeed: 3 },
      { id: 'bar_4', title: 'Golden Slumbers', artist: 'The Beatles', duration: 91, trackNumber: 4, syntheticSeed: 0 }
    ]
  },
  {
    id: 'pink_floyd_dark_side',
    title: 'The Dark Side of the Moon',
    artist: 'Pink Floyd',
    year: 1973,
    artworkUrl: darkSideArt,
    spineColor: '#0a0a0a', // pure black
    spineTextColor: '#e2e8f0',
    catalogNumber: 'CDP 7 46001 2',
    genre: 'Progressive Rock',
    spineStyle: 'pink_floyd_dark_side',
    tracks: [
      { id: 'pf_1', title: 'Speak to Me / Breathe', artist: 'Pink Floyd', duration: 238, trackNumber: 1, syntheticSeed: 3 },
      { id: 'pf_2', title: 'Time', artist: 'Pink Floyd', duration: 413, trackNumber: 2, syntheticSeed: 0 },
      { id: 'pf_3', title: 'Money', artist: 'Pink Floyd', duration: 382, trackNumber: 3, syntheticSeed: 1 },
      { id: 'pf_4', title: 'Us and Them', artist: 'Pink Floyd', duration: 469, trackNumber: 4, syntheticSeed: 2 }
    ]
  },
  {
    id: 'kasabian_st',
    title: 'Kasabian',
    artist: 'Kasabian',
    year: 2004,
    artworkUrl: concreteEchoesArt,
    spineColor: '#171717', // charcoal black
    spineTextColor: '#fca5a5',
    catalogNumber: 'PARADISE 18',
    genre: 'Indie Rock',
    spineStyle: 'kasabian',
    tracks: [
      { id: 'kas_1', title: 'Club Foot', artist: 'Kasabian', duration: 214, trackNumber: 1, syntheticSeed: 1 },
      { id: 'kas_2', title: 'Processed Beats', artist: 'Kasabian', duration: 188, trackNumber: 2, syntheticSeed: 2 },
      { id: 'kas_3', title: 'L.S.F. (Lost Souls Forever)', artist: 'Kasabian', duration: 198, trackNumber: 3, syntheticSeed: 3 }
    ]
  },
  {
    id: 'coldplay_parachutes',
    title: 'Parachutes',
    artist: 'Coldplay',
    year: 2000,
    artworkUrl: midnightBlueArt,
    spineColor: '#0c1a30', // deep navy
    spineTextColor: '#fde047',
    catalogNumber: '5277832',
    genre: 'Alternative',
    spineStyle: 'coldplay_parachutes',
    tracks: [
      { id: 'cp_1', title: 'Don\'t Panic', artist: 'Coldplay', duration: 137, trackNumber: 1, syntheticSeed: 2 },
      { id: 'cp_2', title: 'Shiver', artist: 'Coldplay', duration: 300, trackNumber: 2, syntheticSeed: 0 },
      { id: 'cp_3', title: 'Yellow', artist: 'Coldplay', duration: 269, trackNumber: 3, syntheticSeed: 1 },
      { id: 'cp_4', title: 'Trouble', artist: 'Coldplay', duration: 270, trackNumber: 4, syntheticSeed: 2 }
    ]
  },
  {
    id: 'stone_roses_st',
    title: 'The Stone Roses',
    artist: 'The Stone Roses',
    year: 1989,
    artworkUrl: solarDriftArt,
    spineColor: '#38342e', // ochre / splattered
    spineTextColor: '#fef08a',
    catalogNumber: 'ORE CD 502',
    genre: 'Madchester',
    spineStyle: 'the_stone_roses',
    tracks: [
      { id: 'sr_1', title: 'I Wanna Be Adored', artist: 'The Stone Roses', duration: 292, trackNumber: 1, syntheticSeed: 3 },
      { id: 'sr_2', title: 'She Bangs the Drums', artist: 'The Stone Roses', duration: 232, trackNumber: 2, syntheticSeed: 0 },
      { id: 'sr_3', title: 'Waterfall', artist: 'The Stone Roses', duration: 277, trackNumber: 3, syntheticSeed: 1 }
    ]
  },
  {
    id: 'killers_hot_fuss',
    title: 'Hot Fuss',
    artist: 'The Killers',
    year: 2004,
    artworkUrl: midnightBlueArt,
    spineColor: '#38bdf8', // sky cyan
    spineTextColor: '#0c4a6e',
    catalogNumber: 'LIZARD 007CD',
    genre: 'New Wave / Rock',
    spineStyle: 'the_killers_hot_fuss',
    tracks: [
      { id: 'khf_1', title: 'Jenny Was a Friend of Mine', artist: 'The Killers', duration: 244, trackNumber: 1, syntheticSeed: 0 },
      { id: 'khf_2', title: 'Mr. Brightside', artist: 'The Killers', duration: 222, trackNumber: 2, syntheticSeed: 1 },
      { id: 'khf_3', title: 'Somebody Told Me', artist: 'The Killers', duration: 197, trackNumber: 3, syntheticSeed: 2 }
    ]
  },
  {
    id: 'queen_greatest_hits',
    title: 'Greatest Hits',
    artist: 'Queen',
    year: 1981,
    artworkUrl: velvetTidesArt,
    spineColor: '#881337', // crimson wine
    spineTextColor: '#fef08a',
    catalogNumber: 'CDP 7 46033 2',
    genre: 'Classic Rock',
    spineStyle: 'queen_greatest_hits',
    tracks: [
      { id: 'qgh_1', title: 'Bohemian Rhapsody', artist: 'Queen', duration: 355, trackNumber: 1, syntheticSeed: 2 },
      { id: 'qgh_2', title: 'Another One Bites the Dust', artist: 'Queen', duration: 215, trackNumber: 2, syntheticSeed: 3 },
      { id: 'qgh_3', title: 'Killer Queen', artist: 'Queen', duration: 181, trackNumber: 3, syntheticSeed: 0 }
    ]
  },
  {
    id: 'bowie_hunky_dory',
    title: 'Hunky Dory',
    artist: 'David Bowie',
    year: 1971,
    artworkUrl: solarDriftArt,
    spineColor: '#d6c69f', // sandy cream
    spineTextColor: '#292524',
    catalogNumber: 'PD 83844',
    genre: 'Glam Rock',
    spineStyle: 'david_bowie_hunky_dory',
    tracks: [
      { id: 'bhd_1', title: 'Changes', artist: 'David Bowie', duration: 217, trackNumber: 1, syntheticSeed: 1 },
      { id: 'bhd_2', title: 'Oh! You Pretty Things', artist: 'David Bowie', duration: 192, trackNumber: 2, syntheticSeed: 2 },
      { id: 'bhd_3', title: 'Life on Mars?', artist: 'David Bowie', duration: 228, trackNumber: 3, syntheticSeed: 3 }
    ]
  },
  {
    id: 'pulp_different_class',
    title: 'Different Class',
    artist: 'Pulp',
    year: 1995,
    artworkUrl: concreteEchoesArt,
    spineColor: '#f8fafc', // bright white
    spineTextColor: '#09090b',
    catalogNumber: 'CID 8041',
    genre: 'Britpop',
    spineStyle: 'pulp_different_class',
    tracks: [
      { id: 'pdc_1', title: 'Mis-Shapes', artist: 'Pulp', duration: 226, trackNumber: 1, syntheticSeed: 0 },
      { id: 'pdc_2', title: 'Common People', artist: 'Pulp', duration: 351, trackNumber: 2, syntheticSeed: 1 },
      { id: 'pdc_3', title: 'Disco 2000', artist: 'Pulp', duration: 273, trackNumber: 3, syntheticSeed: 2 }
    ]
  },
  {
    id: 'arctic_monkeys_am',
    title: 'AM',
    artist: 'Arctic Monkeys',
    year: 2013,
    artworkUrl: darkSideArt,
    spineColor: '#09090b', // obsidian black
    spineTextColor: '#fafafa',
    catalogNumber: 'WIGCD317',
    genre: 'Indie Rock',
    spineStyle: 'arctic_monkeys_am',
    tracks: [
      { id: 'am_1', title: 'Do I Wanna Know?', artist: 'Arctic Monkeys', duration: 272, trackNumber: 1, syntheticSeed: 3 },
      { id: 'am_2', title: 'R U Mine?', artist: 'Arctic Monkeys', duration: 201, trackNumber: 2, syntheticSeed: 0 },
      { id: 'am_3', title: 'Why\'d You Only Call Me When You\'re High?', artist: 'Arctic Monkeys', duration: 161, trackNumber: 3, syntheticSeed: 1 }
    ]
  },
  {
    id: 'muse_origin_of_symmetry',
    title: 'Origin of Symmetry',
    artist: 'Muse',
    year: 2001,
    artworkUrl: solarDriftArt,
    spineColor: '#f97316', // bright amber
    spineTextColor: '#ffffff',
    catalogNumber: 'MUSH93CD',
    genre: 'Alternative Rock',
    spineStyle: 'muse_origin_of_symmetry',
    tracks: [
      { id: 'mo_1', title: 'New Born', artist: 'Muse', duration: 363, trackNumber: 1, syntheticSeed: 2 },
      { id: 'mo_2', title: 'Bliss', artist: 'Muse', duration: 252, trackNumber: 2, syntheticSeed: 3 },
      { id: 'mo_3', title: 'Plug In Baby', artist: 'Muse', duration: 220, trackNumber: 3, syntheticSeed: 0 }
    ]
  },
  {
    id: 'the_vaccines_st',
    title: 'What Did You Expect from The Vaccines?',
    artist: 'The Vaccines',
    year: 2011,
    artworkUrl: velvetTidesArt,
    spineColor: '#e2e8f0', // soft white / red
    spineTextColor: '#991b1b',
    catalogNumber: '88697843472',
    genre: 'Post-Punk Revival',
    spineStyle: 'the_vaccines',
    tracks: [
      { id: 'vac_1', title: 'Wreckin\' Bar (Ra Ra Ra)', artist: 'The Vaccines', duration: 84, trackNumber: 1, syntheticSeed: 1 },
      { id: 'vac_2', title: 'If You Wanna', artist: 'The Vaccines', duration: 182, trackNumber: 2, syntheticSeed: 2 },
      { id: 'vac_3', title: 'Post Break-Up Sex', artist: 'The Vaccines', duration: 176, trackNumber: 3, syntheticSeed: 3 }
    ]
  },
  {
    id: 'oasis_morning_glory',
    title: '(What\'s the Story) Morning Glory?',
    artist: 'Oasis',
    year: 1995,
    artworkUrl: definitelyMaybeArt,
    spineColor: '#0a1628', // midnight navy
    spineTextColor: '#e2e8f0',
    catalogNumber: 'CRECD 189',
    genre: 'Britpop',
    spineStyle: 'oasis_morning_glory',
    tracks: [
      { id: 'omg_1', title: 'Hello', artist: 'Oasis', duration: 201, trackNumber: 1, syntheticSeed: 0 },
      { id: 'omg_2', title: 'Wonderwall', artist: 'Oasis', duration: 258, trackNumber: 2, syntheticSeed: 1 },
      { id: 'omg_3', title: 'Don\'t Look Back in Anger', artist: 'Oasis', duration: 288, trackNumber: 3, syntheticSeed: 2 },
      { id: 'omg_4', title: 'Champagne Supernova', artist: 'Oasis', duration: 449, trackNumber: 4, syntheticSeed: 3 }
    ]
  },
  {
    id: 'charlatans_tellin_stories',
    title: 'Tellin\' Stories',
    artist: 'The Charlatans',
    year: 1997,
    artworkUrl: concreteEchoesArt,
    spineColor: '#f1f5f9', // chalk white
    spineTextColor: '#111827',
    catalogNumber: 'BBQCD 190',
    genre: 'Indie Rock',
    spineStyle: 'the_charlatans',
    tracks: [
      { id: 'cts_1', title: 'One to Another', artist: 'The Charlatans', duration: 270, trackNumber: 1, syntheticSeed: 1 },
      { id: 'cts_2', title: 'North Country Boy', artist: 'The Charlatans', duration: 244, trackNumber: 2, syntheticSeed: 2 }
    ]
  },
  {
    id: 'rem_automatic',
    title: 'Automatic for the People',
    artist: 'R.E.M.',
    year: 1992,
    artworkUrl: midnightBlueArt,
    spineColor: '#374151', // slate grey
    spineTextColor: '#f3f4f6',
    catalogNumber: '9362-45055-2',
    genre: 'Alternative',
    spineStyle: 'rem_automatic',
    tracks: [
      { id: 'rem_1', title: 'Drive', artist: 'R.E.M.', duration: 271, trackNumber: 1, syntheticSeed: 3 },
      { id: 'rem_2', title: 'Everybody Hurts', artist: 'R.E.M.', duration: 318, trackNumber: 2, syntheticSeed: 0 },
      { id: 'rem_3', title: 'Man on the Moon', artist: 'R.E.M.', duration: 313, trackNumber: 3, syntheticSeed: 1 }
    ]
  }
];
