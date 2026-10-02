import fs from 'fs';
import path from 'path';

// Generate 192x192 and 512x512 SVG icons in public/
const svg192 = `<svg xmlns="http://www.w3.org/2000/svg" width="192" height="192" viewBox="0 0 192 192">
  <rect width="192" height="192" rx="36" fill="#121212"/>
  <rect x="24" y="24" width="144" height="144" rx="8" fill="#1E1D1B" stroke="#ffffff" stroke-opacity="0.15" stroke-width="2"/>
  <rect x="24" y="24" width="20" height="144" rx="4" fill="#2A2825" stroke="#ffffff" stroke-opacity="0.1" stroke-width="1"/>
  <circle cx="106" cy="96" r="48" fill="none" stroke="#E5E5E5" stroke-width="3"/>
  <circle cx="106" cy="96" r="16" fill="#121212" stroke="#FFFFFF" stroke-width="2.5"/>
  <circle cx="106" cy="96" r="7" fill="#262626"/>
  <text x="34" y="100" fill="#D4AF37" font-size="8" font-family="monospace" font-weight="bold" transform="rotate(-90 34 100)" text-anchor="middle">SPINE</text>
</svg>`;

const svg512 = `<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512">
  <rect width="512" height="512" rx="96" fill="#121212"/>
  <rect x="64" y="64" width="384" height="384" rx="20" fill="#1E1D1B" stroke="#ffffff" stroke-opacity="0.15" stroke-width="4"/>
  <rect x="64" y="64" width="52" height="384" rx="10" fill="#2A2825" stroke="#ffffff" stroke-opacity="0.1" stroke-width="2"/>
  <circle cx="280" cy="256" r="128" fill="none" stroke="#E5E5E5" stroke-width="8"/>
  <circle cx="280" cy="256" r="42" fill="#121212" stroke="#FFFFFF" stroke-width="6"/>
  <circle cx="280" cy="256" r="18" fill="#262626"/>
  <text x="90" y="260" fill="#D4AF37" font-size="20" font-family="monospace" font-weight="bold" transform="rotate(-90 90 260)" text-anchor="middle">SPINE</text>
</svg>`;

fs.writeFileSync(path.resolve('public', 'icon-192.svg'), svg192);
fs.writeFileSync(path.resolve('public', 'icon-512.svg'), svg512);

// Also copy or provide as png if needed
fs.writeFileSync(path.resolve('public', 'icon-192.png'), svg192);
fs.writeFileSync(path.resolve('public', 'icon-512.png'), svg512);

console.log('Icons created in public/');
