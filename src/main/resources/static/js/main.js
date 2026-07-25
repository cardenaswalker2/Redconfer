// Main JS for REDCONFER Premium Platform
document.addEventListener('DOMContentLoaded', () => {
    // 1. Dark Mode System
    const toggleBtn = document.getElementById('darkModeToggle');
    const toggleIcon = document.getElementById('darkModeIcon');
    const currentTheme = localStorage.getItem('theme') || 'light';

    // Apply saved theme
    if (currentTheme === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
        toggleIcon.className = 'bi bi-sun-fill';
    } else {
        document.documentElement.setAttribute('data-theme', 'light');
        toggleIcon.className = 'bi bi-moon-fill';
    }

    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            let theme = document.documentElement.getAttribute('data-theme');
            if (theme === 'dark') {
                document.documentElement.setAttribute('data-theme', 'light');
                toggleIcon.className = 'bi bi-moon-fill';
                localStorage.setItem('theme', 'light');
            } else {
                document.documentElement.setAttribute('data-theme', 'dark');
                toggleIcon.className = 'bi bi-sun-fill';
                localStorage.setItem('theme', 'dark');
            }
        });
    }

    // 2. Before/After Image Sliders
    const sliders = document.querySelectorAll('.comparison-slider');
    sliders.forEach(slider => {
        const resizeImg = slider.querySelector('.resize-img');
        const handle = slider.querySelector('.handle');
        
        if (!resizeImg || !handle) return;

        let active = false;

        const slide = (x) => {
            let rect = slider.getBoundingClientRect();
            let position = ((x - rect.left) / rect.width) * 100;
            if (position < 0) position = 0;
            if (position > 100) position = 100;
            
            resizeImg.style.width = `${position}%`;
            handle.style.left = `${position}%`;
        };

        // Desktop
        slider.addEventListener('mousedown', () => active = true);
        window.addEventListener('mouseup', () => active = false);
        slider.addEventListener('mousemove', (e) => {
            if (!active) return;
            slide(e.pageX);
        });

        // Mobile
        slider.addEventListener('touchstart', () => active = true);
        window.addEventListener('touchend', () => active = false);
        slider.addEventListener('touchmove', (e) => {
            if (!active) return;
            slide(e.touches[0].pageX);
        });
    });
});
