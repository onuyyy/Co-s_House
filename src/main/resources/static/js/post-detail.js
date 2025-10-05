// 게시글 상세 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    const sliderItems = document.querySelectorAll('.slider-item');
    const sliderIndicator = document.getElementById('sliderIndicator');
    const prevBtn = document.querySelector('.slider-btn.prev');
    const nextBtn = document.querySelector('.slider-btn.next');
    
    let currentSlide = 0;
    const totalSlides = sliderItems.length;

    // 슬라이드가 2개 이상일 때만 버튼과 인디케이터 표시
    if (totalSlides > 1) {
        prevBtn.style.display = 'block';
        nextBtn.style.display = 'block';

        // 인디케이터 생성
        for (let i = 0; i < totalSlides; i++) {
            const dot = document.createElement('span');
            dot.onclick = () => goToSlide(i);
            if (i === 0) dot.classList.add('active');
            sliderIndicator.appendChild(dot);
        }
    }

    // 슬라이드 이동
    window.goToSlide = function(index) {
        index = parseInt(index);
        currentSlide = index;

        // 모든 슬라이드 비활성화
        sliderItems.forEach(item => item.classList.remove('active'));
        
        // 현재 슬라이드 활성화
        sliderItems[currentSlide].classList.add('active');

        // 인디케이터 업데이트
        const dots = sliderIndicator.querySelectorAll('span');
        dots.forEach((dot, i) => {
            dot.classList.toggle('active', i === currentSlide);
        });

        // 썸네일 업데이트
        const thumbnails = document.querySelectorAll('.thumbnail-item');
        thumbnails.forEach((thumb, i) => {
            thumb.classList.toggle('active', i === currentSlide);
        });
    };

    // 이전 슬라이드
    window.prevSlide = function() {
        currentSlide = (currentSlide - 1 + totalSlides) % totalSlides;
        goToSlide(currentSlide);
    };

    // 다음 슬라이드
    window.nextSlide = function() {
        currentSlide = (currentSlide + 1) % totalSlides;
        goToSlide(currentSlide);
    };

    // 키보드 네비게이션
    document.addEventListener('keydown', function(e) {
        if (totalSlides > 1) {
            if (e.key === 'ArrowLeft') {
                prevSlide();
            } else if (e.key === 'ArrowRight') {
                nextSlide();
            }
        }
    });

    // 터치 스와이프 지원
    let touchStartX = 0;
    let touchEndX = 0;
    
    const sliderMain = document.querySelector('.slider-main');
    
    if (sliderMain && totalSlides > 1) {
        sliderMain.addEventListener('touchstart', function(e) {
            touchStartX = e.changedTouches[0].screenX;
        });

        sliderMain.addEventListener('touchend', function(e) {
            touchEndX = e.changedTouches[0].screenX;
            handleSwipe();
        });

        function handleSwipe() {
            const swipeThreshold = 50;
            if (touchEndX < touchStartX - swipeThreshold) {
                // 왼쪽으로 스와이프 (다음)
                nextSlide();
            }
            if (touchEndX > touchStartX + swipeThreshold) {
                // 오른쪽으로 스와이프 (이전)
                prevSlide();
            }
        }
    }
});
