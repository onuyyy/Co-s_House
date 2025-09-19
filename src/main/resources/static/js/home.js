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
})();

