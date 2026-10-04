import React, { useRef, useEffect, useCallback } from 'react';
import { Release } from '../types/music';
import { audioEngine } from '../audio/audioEngine';
import { PhysicalCdSpine } from './PhysicalCdSpine';
import woodTexture from '../assets/images/shelf_walnut_wood_1790946365885.jpg';

interface CdShelfProps {
  releases: Release[];
  selectedIndex: number;
  onSelectRelease: (index: number) => void;
  shelfHeightClass?: string;
}

interface VelocityPoint {
  x: number;
  time: number;
}

export const CdShelf: React.FC<CdShelfProps> = ({
  releases,
  selectedIndex,
  onSelectRelease,
  shelfHeightClass = 'h-[310px] sm:h-[330px]'
}) => {
  const containerRef = useRef<HTMLDivElement>(null);

  // Interaction & Physics state
  const isDraggingRef = useRef(false);
  const startXRef = useRef(0);
  const startScrollLeftRef = useRef(0);
  const velocityHistoryRef = useRef<VelocityPoint[]>([]);
  const animFrameIdRef = useRef<number | null>(null);
  const snapAnimIdRef = useRef<number | null>(null);
  const lastCenteredIndexRef = useRef(selectedIndex);
  const isExternalUpdateRef = useRef(false);

  // Sync ref with prop
  useEffect(() => {
    lastCenteredIndexRef.current = selectedIndex;
  }, [selectedIndex]);

  // Subtle single haptic click on index change
  const triggerHaptic = useCallback(() => {
    try {
      if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
        navigator.vibrate(6); // Crisp 6ms micro-pulse
      }
    } catch {}
  }, []);

  // Compute scroll offset to put a given spine dead-center
  const getCenterScrollForIndex = useCallback((index: number) => {
    const container = containerRef.current;
    if (!container) return 0;
    const spineElements = container.querySelectorAll<HTMLElement>('.cd-spine-item');
    const target = spineElements[index];
    if (!target) return 0;

    const containerWidth = container.clientWidth;
    return target.offsetLeft - (containerWidth / 2) + (target.offsetWidth / 2);
  }, []);

  // Check which CD is closest to the horizontal center during conveyor movement
  const checkAndUpdateCenterSelection = useCallback(() => {
    const container = containerRef.current;
    if (!container) return;

    const containerCenter = container.scrollLeft + container.clientWidth / 2;
    const spineElements = container.querySelectorAll<HTMLElement>('.cd-spine-item');
    if (spineElements.length === 0) return;

    let closestIdx = 0;
    let minDistance = Infinity;

    spineElements.forEach((el, idx) => {
      const elCenter = el.offsetLeft + el.offsetWidth / 2;
      const dist = Math.abs(containerCenter - elCenter);
      if (dist < minDistance) {
        minDistance = dist;
        closestIdx = idx;
      }
    });

    if (closestIdx !== lastCenteredIndexRef.current) {
      lastCenteredIndexRef.current = closestIdx;
      triggerHaptic();
      audioEngine.playSpineSlideSound();
      onSelectRelease(closestIdx);
    }
  }, [onSelectRelease, triggerHaptic]);

  // Smooth ease-out animation to settle precisely on the center
  const animateSnapTo = useCallback((targetScroll: number, durationMs = 240) => {
    const container = containerRef.current;
    if (!container) return;

    if (snapAnimIdRef.current) {
      cancelAnimationFrame(snapAnimIdRef.current);
      snapAnimIdRef.current = null;
    }

    const startScroll = container.scrollLeft;
    const distance = targetScroll - startScroll;
    if (Math.abs(distance) < 0.5) return;

    const startTime = performance.now();

    const step = (now: number) => {
      const elapsed = now - startTime;
      const progress = Math.min(1, elapsed / durationMs);
      // Quintic ease-out for ultra-smooth physical settling
      const ease = 1 - Math.pow(1 - progress, 4);

      container.scrollLeft = startScroll + distance * ease;

      if (progress < 1) {
        snapAnimIdRef.current = requestAnimationFrame(step);
      } else {
        container.scrollLeft = targetScroll;
        snapAnimIdRef.current = null;
      }
    };

    snapAnimIdRef.current = requestAnimationFrame(step);
  }, []);

  // When selectedIndex changes programmatically (e.g. orientation change, Next/Prev button, search)
  useEffect(() => {
    if (isDraggingRef.current || animFrameIdRef.current !== null) return;
    const targetScroll = getCenterScrollForIndex(selectedIndex);
    const container = containerRef.current;
    if (container) {
      container.scrollLeft = targetScroll;
      lastCenteredIndexRef.current = selectedIndex;
    }
  }, [selectedIndex, getCenterScrollForIndex]);

  // Cancel any running animations
  const stopAllAnimations = () => {
    if (animFrameIdRef.current !== null) {
      cancelAnimationFrame(animFrameIdRef.current);
      animFrameIdRef.current = null;
    }
    if (snapAnimIdRef.current !== null) {
      cancelAnimationFrame(snapAnimIdRef.current);
      snapAnimIdRef.current = null;
    }
  };

  // --- CONVEYOR BELT POINTER DRAGGING & FLICK MOMENTUM ---

  const handlePointerDown = (clientX: number) => {
    stopAllAnimations();
    isDraggingRef.current = true;
    startXRef.current = clientX;
    startScrollLeftRef.current = containerRef.current?.scrollLeft || 0;

    const now = performance.now();
    velocityHistoryRef.current = [{ x: clientX, time: now }];
  };

  const handlePointerMove = (clientX: number) => {
    if (!isDraggingRef.current || !containerRef.current) return;

    const deltaX = clientX - startXRef.current;
    // 1:1 direct tracking: finger movement directly pulls the conveyor belt
    containerRef.current.scrollLeft = startScrollLeftRef.current - deltaX;

    // Record velocity history (keep last 80ms)
    const now = performance.now();
    velocityHistoryRef.current.push({ x: clientX, time: now });
    if (velocityHistoryRef.current.length > 6) {
      velocityHistoryRef.current.shift();
    }

    // Immediately detect which CD is currently passing the center
    checkAndUpdateCenterSelection();
  };

  const handlePointerUp = () => {
    if (!isDraggingRef.current || !containerRef.current) return;
    isDraggingRef.current = false;

    // Compute release velocity (px per ms) from recent history
    const history = velocityHistoryRef.current;
    let velocity = 0;
    if (history.length >= 2) {
      const oldest = history[0];
      const newest = history[history.length - 1];
      const dt = newest.time - oldest.time;
      if (dt > 10 && dt < 200) {
        velocity = (newest.x - oldest.x) / dt; // positive means dragged right (scrolling left)
      }
    }

    // If flicked with sufficient momentum: simulate physical deceleration
    if (Math.abs(velocity) > 0.25) {
      let currentVelocity = -velocity * 16; // convert to px/frame (negative because drag right = scroll left)
      let lastTime = performance.now();

      const momentumStep = (now: number) => {
        const dt = Math.min(32, now - lastTime);
        lastTime = now;

        if (!containerRef.current) return;

        // Apply friction
        const friction = Math.pow(0.93, dt / 16);
        currentVelocity *= friction;

        containerRef.current.scrollLeft += currentVelocity;
        checkAndUpdateCenterSelection();

        // Continue until velocity falls below threshold
        if (Math.abs(currentVelocity) > 0.4) {
          animFrameIdRef.current = requestAnimationFrame(momentumStep);
        } else {
          // Movement ceases: DO NOT SNAP!
          // Whichever CD is at the center line at this exact moment stays selected.
          animFrameIdRef.current = null;
        }
      };

      animFrameIdRef.current = requestAnimationFrame(momentumStep);
    } else {
      // Gentle release: DO NOT SNAP!
      // Leave the CD shelf exactly where it is.
    }
  };

  // Mouse Handlers
  const onMouseDown = (e: React.MouseEvent) => {
    handlePointerDown(e.pageX);
  };
  const onMouseMove = (e: React.MouseEvent) => {
    handlePointerMove(e.pageX);
  };
  const onMouseUp = () => {
    handlePointerUp();
  };

  // Touch Handlers
  const onTouchStart = (e: React.TouchEvent) => {
    handlePointerDown(e.touches[0].clientX);
  };
  const onTouchMove = (e: React.TouchEvent) => {
    handlePointerMove(e.touches[0].clientX);
  };
  const onTouchEnd = () => {
    handlePointerUp();
  };

  return (
    <div
      className={`relative w-full select-none overflow-hidden flex flex-col justify-between bg-[#0e0c0a] cursor-grab active:cursor-grabbing touch-pan-x ${shelfHeightClass}`}
    >
      {/* 1. TOP WOODEN SHELF PLANK */}
      <div
        className="w-full h-3 sm:h-3.5 relative z-30 shadow-[0_3px_8px_rgba(0,0,0,0.8)] border-b border-[#5c3a21]/60 shrink-0 pointer-events-none"
        style={{
          backgroundImage: `url(${woodTexture})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center 20%'
        }}
      >
        <div className="absolute inset-x-0 bottom-0 h-[1px] bg-gradient-to-r from-amber-950 via-amber-800 to-amber-950 opacity-80" />
        <div className="absolute inset-x-0 bottom-0 h-2 bg-gradient-to-b from-transparent to-black/60 pointer-events-none" />
      </div>

      {/* 2. INNER SHELF CAVITY: CONTINUOUS CONVEYOR BELT OF CD JEWEL CASES */}
      <div className="flex-1 w-full relative flex items-center overflow-hidden">
        {/* Dark walnut grain backing */}
        <div
          className="absolute inset-0 opacity-40 mix-blend-luminosity bg-cover bg-center pointer-events-none"
          style={{ backgroundImage: `url(${woodTexture})` }}
        />

        {/* Ambient Cavity Shadows */}
        <div className="absolute inset-x-0 top-0 h-10 bg-gradient-to-b from-black/85 via-black/35 to-transparent pointer-events-none z-10" />
        <div className="absolute inset-x-0 bottom-0 h-6 bg-gradient-to-t from-black/70 to-transparent pointer-events-none z-10" />

        {/* The Continuous Conveyor Belt Container (Bottom-aligned so CD cases rest directly on the wooden shelf) */}
        <div
          ref={containerRef}
          onMouseDown={onMouseDown}
          onMouseMove={onMouseMove}
          onMouseUp={onMouseUp}
          onMouseLeave={onMouseUp}
          onTouchStart={onTouchStart}
          onTouchMove={onTouchMove}
          onTouchEnd={onTouchEnd}
          onTouchCancel={onTouchEnd}
          className="w-full h-full overflow-x-auto scrollbar-none flex items-end relative z-20 pb-0"
          style={{
            // Center padding allows every single CD to sit dead-center
            paddingLeft: 'calc(50% - 11.25px)',
            paddingRight: 'calc(50% - 11.25px)'
          }}
        >
          <div className="flex items-end gap-[1.5px] select-none">
            {releases.map((rel, index) => {
              const isSelected = index === selectedIndex;
              return (
                <PhysicalCdSpine
                  key={rel.id}
                  release={rel}
                  isSelected={isSelected}
                  onClick={() => {
                    // Optional direct click fallback
                    const targetScroll = getCenterScrollForIndex(index);
                    animateSnapTo(targetScroll, 240);
                    if (index !== lastCenteredIndexRef.current) {
                      lastCenteredIndexRef.current = index;
                      triggerHaptic();
                      audioEngine.playSpineSlideSound();
                      onSelectRelease(index);
                    }
                  }}
                />
              );
            })}
          </div>
        </div>

        {/* Subtle physical contact shadow where CD case bases meet the wooden shelf */}
        <div className="absolute inset-x-0 bottom-0 h-2 bg-gradient-to-t from-black/80 via-black/40 to-transparent pointer-events-none z-25" />
      </div>

      {/* 3. BOTTOM SOLID WOODEN SHELF BASE (Raised solid wooden ledge) */}
      <div
        className="w-full h-6 sm:h-6.5 relative z-30 shadow-[0_-4px_20px_rgba(0,0,0,0.9)] border-t border-[#6b4426]/60 shrink-0 pointer-events-none"
        style={{
          backgroundImage: `url(${woodTexture})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center 60%'
        }}
      >
        <div className="absolute inset-x-0 top-0 h-[2px] bg-gradient-to-r from-amber-900/60 via-amber-500/80 to-amber-900/60 shadow-xs" />
        <div className="absolute inset-x-0 top-0 h-2.5 bg-gradient-to-b from-black/70 to-transparent pointer-events-none" />
        <div className="absolute inset-x-0 bottom-0 h-[1px] bg-black/90" />
      </div>
    </div>
  );
};
