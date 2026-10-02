/**
 * IndexedDB local cache for album artwork to persist downloaded Cover Art Archive images
 * and embedded ID3 pictures across browser sessions.
 */

const DB_NAME = 'spine_artwork_cache';
const STORE_NAME = 'artworks';
const DB_VERSION = 1;

function openDB(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    if (typeof window === 'undefined' || !window.indexedDB) {
      reject(new Error('IndexedDB not supported'));
      return;
    }
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: 'key' });
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

export async function cacheArtwork(key: string, blobUrlOrData: string): Promise<void> {
  try {
    const db = await openDB();
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);
    store.put({ key, data: blobUrlOrData, timestamp: Date.now() });
  } catch (e) {
    console.warn('Failed to cache artwork:', e);
  }
}

export async function getCachedArtwork(key: string): Promise<string | null> {
  try {
    const db = await openDB();
    return new Promise((resolve) => {
      const tx = db.transaction(STORE_NAME, 'readonly');
      const store = tx.objectStore(STORE_NAME);
      const req = store.get(key);
      req.onsuccess = () => {
        resolve(req.result ? req.result.data : null);
      };
      req.onerror = () => resolve(null);
    });
  } catch {
    return null;
  }
}
