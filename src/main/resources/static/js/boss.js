document.addEventListener('DOMContentLoaded', () => {
    const gameBox = document.querySelector('.boss-game-box');
    const startScreen = document.getElementById('startScreen');
    const guideScreen = document.getElementById('guideScreen');
    const guideContent = document.getElementById('guideContent');

    let currentStep = 1;

    gameBox.addEventListener('click', () => {
        if (currentStep === 1) {
            showGuideScreen();
            currentStep = 2;
        }
    });

    function showGuideScreen() {
        startScreen.classList.remove('active');
        guideScreen.classList.add('active');

        gameBox.classList.add('dark');

        guideContent.classList.remove('show');

        requestAnimationFrame(() => {
            guideContent.classList.add('show');
        });
    }
});