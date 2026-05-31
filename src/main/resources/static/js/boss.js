document.addEventListener('DOMContentLoaded', () => {
    const gameBox = document.getElementById('bossGameBox');

    const startScreen = document.getElementById('startScreen');
    const guideScreen = document.getElementById('guideScreen');
    const playScreen = document.getElementById('playScreen');

    const guideContent = document.getElementById('guideContent');
    const countdownText = document.getElementById('countdownText');
    const playTimer = document.getElementById('playTimer');

    let currentStep = 'start';
    let isTransitioning = false;

    gameBox.addEventListener('click', handleScreenClick);

    function handleScreenClick() {
        if (isTransitioning) {
            return;
        }

        if (currentStep === 'start') {
            showGuideScreen();
            return;
        }

        if (currentStep === 'guide') {
            showPlayScreen();
            startCountdownSequence();
        }
    }

    function showGuideScreen() {
        startScreen.classList.remove('active');
        playScreen.classList.remove('active');

        guideScreen.classList.add('active');

        guideContent.classList.remove('show');
        void guideContent.offsetWidth;
        guideContent.classList.add('show');

        currentStep = 'guide';
    }

    function showPlayScreen() {
        startScreen.classList.remove('active');
        guideScreen.classList.remove('active');

        playScreen.classList.add('active');
        playTimer.textContent = '00:15';

        currentStep = 'play';
    }

    async function startCountdownSequence() {
        isTransitioning = true;

        const countdownItems = [
            { text: '3', className: 'countdown-three', duration: 900 },
            { text: '2', className: 'countdown-two', duration: 900 },
            { text: '1', className: 'countdown-one', duration: 900 },
            { text: 'START !', className: 'countdown-start', duration: 1100 }
        ];

        for (const item of countdownItems) {
            showCountdown(item.text, item.className);
            await wait(item.duration);
        }

        resetCountdown();
        isTransitioning = false;

        // 나중에 여기서 실제 15초 게임 시작 로직 붙이면 됨
        // startRealGame();
    }

    function showCountdown(text, className) {
        countdownText.className = 'countdown-text';
        countdownText.textContent = text;

        void countdownText.offsetWidth;

        countdownText.classList.add(className);
        countdownText.classList.add('show');
    }

    function resetCountdown() {
        countdownText.className = 'countdown-text';
        countdownText.textContent = '';
    }

    function wait(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
});