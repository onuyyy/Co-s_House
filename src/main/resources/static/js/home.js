// Home page small interactions
(function(){
  const cats = document.querySelector('.home-cats .cats');
  if (cats) {
    // Mouse wheel horizontal scroll on categories
    cats.addEventListener('wheel', (e)=>{
      if (Math.abs(e.deltaY) > Math.abs(e.deltaX)) {
        cats.scrollLeft += e.deltaY * 0.6;
        e.preventDefault();
      }
    }, { passive:false });
  }

  // Smooth anchor scrolling for in-page links
  document.querySelectorAll('a[href^="#"]').forEach(a => {
    a.addEventListener('click', (e)=>{
      const id = a.getAttribute('href');
      try {
        const el = document.querySelector(id);
        if (el) {
          e.preventDefault();
          el.scrollIntoView({ behavior:'smooth', block:'start' });
        }
      } catch(_){}
    });
  });

  // Events slider
  const slider = document.querySelector('[data-event-slider]');
  if (!slider) {
    return;
  }

  const track = slider.querySelector('[data-event-track]');
  const slides = track ? Array.from(track.children) : [];
  if (!track || slides.length === 0) {
    return;
  }

  if (slides.length <= 1) {
    slider.classList.add('is-single');
    return;
  }

  const prevBtn = slider.querySelector('[data-event-prev]');
  const nextBtn = slider.querySelector('[data-event-next]');
  const dots = Array.from(slider.querySelectorAll('[data-event-dot]'));
  const AUTO_INTERVAL = 6000;
  let currentIndex = 0;
  let autoTimer = null;

  const update = (index) => {
    currentIndex = (index + slides.length) % slides.length;
    track.style.transform = `translateX(-${currentIndex * 100}%)`;
    dots.forEach((dot, idx) => {
      dot.classList.toggle('is-active', idx === currentIndex);
      dot.setAttribute('aria-pressed', idx === currentIndex ? 'true' : 'false');
    });
  };

  const goNext = () => update(currentIndex + 1);
  const goPrev = () => update(currentIndex - 1);

  const stopAuto = () => {
    if (autoTimer) {
      clearInterval(autoTimer);
      autoTimer = null;
    }
  };

  const startAuto = () => {
    stopAuto();
    autoTimer = setInterval(goNext, AUTO_INTERVAL);
  };

  prevBtn?.addEventListener('click', () => {
    goPrev();
    startAuto();
  });

  nextBtn?.addEventListener('click', () => {
    goNext();
    startAuto();
  });

  dots.forEach((dot, idx) => {
    dot.addEventListener('click', () => {
      update(idx);
      startAuto();
    });
  });

  slider.addEventListener('mouseenter', stopAuto);
  slider.addEventListener('mouseleave', startAuto);

  slider.addEventListener('touchstart', stopAuto, { passive: true });
  slider.addEventListener('touchend', startAuto, { passive: true });

  document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
      stopAuto();
    } else {
      startAuto();
    }
  });

  update(0);
  startAuto();
})();
