document.addEventListener('DOMContentLoaded', () => {
    const gameBox = document.querySelector('.boss-game-box');
    const startScreen = document.getElementById('startScreen');
    const guideScreen = document.getElementById('guideScreen');

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
    }
});