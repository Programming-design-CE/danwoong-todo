document.addEventListener('DOMContentLoaded', () => {
    const gameBox = document.getElementById('bossGameBox');

    const startScreen = document.getElementById('startScreen');
    const guideScreen = document.getElementById('guideScreen');
    const playScreen = document.getElementById('playScreen');

    const guideContent = document.getElementById('guideContent');

    const playTimer = document.getElementById('playTimer');
    const countdownText = document.getElementById('countdownText');
    const hitCount = document.getElementById('hitCount');
    const finishText = document.getElementById('finishText');

    const danbiNormal = document.getElementById('danbiNormal');
    const danbiMouth = document.getElementById('danbiMouth');

    const handWithGarlic = document.getElementById('handWithGarlic');
    const handWithoutGarlic = document.getElementById('handWithoutGarlic');

    let currentStep = 'start';
    let gamePhase = 'idle';

    let hitTotal = 0;
    let gameTimerInterval = null;
    let gameEndTime = 0;
    let feedAnimationTimeouts = [];

    const GAME_DURATION = 15000;

    gameBox.addEventListener('click', handleGameBoxClick);

    function handleGameBoxClick() {
        if (currentStep === 'start') {
            showGuideScreen();
            return;
        }

        if (currentStep === 'guide') {
            showPlayScreen();
            startCountdownSequence();
            return;
        }

        if (currentStep === 'play' && gamePhase === 'playing') {
            handleGameHit();
        }
    }

    function showGuideScreen() {
        activateScreen(guideScreen);
        currentStep = 'guide';

        guideContent.classList.remove('show');
        void guideContent.offsetWidth;
        guideContent.classList.add('show');
    }

    function showPlayScreen() {
        activateScreen(playScreen);

        currentStep = 'play';
        gamePhase = 'countdown';

        hitTotal = 0;

        playTimer.textContent = '00:15';
        playTimer.classList.remove('timer-warning', 'timer-danger');

        countdownText.textContent = '';
        countdownText.className = 'countdown-text';

        hitCount.textContent = 'x0';
        hitCount.classList.remove('visible', 'pop');

        finishText.classList.remove('show');

        resetCharactersToIdle();
    }

    function activateScreen(targetScreen) {
        startScreen.classList.remove('active');
        guideScreen.classList.remove('active');
        playScreen.classList.remove('active');

        targetScreen.classList.add('active');
    }

    async function startCountdownSequence() {
        const countdownItems = [
            { text: '3', className: 'countdown-three', duration: 850 },
            { text: '2', className: 'countdown-two', duration: 850 },
            { text: '1', className: 'countdown-one', duration: 850 },
            { text: 'START !', className: 'countdown-start', duration: 1000 }
        ];

        for (const item of countdownItems) {
            showCountdown(item.text, item.className);
            await wait(item.duration);
        }

        resetCountdown();
        startRealGame();
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

    function startRealGame() {
        gamePhase = 'playing';
        hitTotal = 0;
        gameEndTime = Date.now() + GAME_DURATION;

        hitCount.textContent = 'x0';
        hitCount.classList.remove('visible', 'pop');

        playTimer.classList.remove('timer-warning', 'timer-danger');
        updateTimerDisplay(GAME_DURATION);

        if (gameTimerInterval) {
            clearInterval(gameTimerInterval);
        }

        gameTimerInterval = setInterval(() => {
            const remainingMs = gameEndTime - Date.now();

            if (remainingMs <= 0) {
                finishGame();
                return;
            }

            updateTimerDisplay(remainingMs);
        }, 100);
    }

    function updateTimerDisplay(remainingMs) {
        const seconds = Math.ceil(remainingMs / 1000);
        const safeSeconds = Math.max(0, seconds);

        playTimer.textContent = `00:${String(safeSeconds).padStart(2, '0')}`;

        playTimer.classList.remove('timer-warning', 'timer-danger');

        if (remainingMs <= 5000) {
            playTimer.classList.add('timer-danger');
        }
    }

    function handleGameHit() {
        hitTotal += 1;

        hitCount.textContent = `x${hitTotal}`;
        hitCount.classList.add('visible');

        hitCount.classList.remove('pop');
        void hitCount.offsetWidth;
        hitCount.classList.add('pop');

        runFeedAnimation();
    }

    function runFeedAnimation() {
        clearFeedAnimationTimeouts();

        resetCharactersToIdle();
        void handWithGarlic.offsetWidth;

        // 1단계: 마늘 든 손이 단비 입 쪽으로 이동, 단비 입벌림
        danbiNormal.classList.remove('visible-state');
        danbiMouth.classList.add('visible-state');

        handWithGarlic.classList.add('visible-state');
        handWithGarlic.classList.add('hit-move');

        handWithoutGarlic.classList.remove('visible-state');
        handWithoutGarlic.classList.remove('after-move');

        const firstTimeout = setTimeout(() => {
            // 2단계: 마늘 없는 손으로 교체
            danbiMouth.classList.remove('visible-state');
            danbiNormal.classList.add('visible-state');

            handWithGarlic.classList.remove('visible-state');
            handWithGarlic.classList.remove('hit-move');

            handWithoutGarlic.classList.add('visible-state');
            handWithoutGarlic.classList.add('after-move');
        }, 110);

        const secondTimeout = setTimeout(() => {
            // 3단계: 원래 손으로 복귀
            resetCharactersToIdle();
        }, 240);

        feedAnimationTimeouts.push(firstTimeout, secondTimeout);
    }

    function resetCharactersToIdle() {
        danbiNormal.classList.add('visible-state');
        danbiMouth.classList.remove('visible-state');

        handWithGarlic.classList.add('visible-state');
        handWithGarlic.classList.remove('hit-move');

        handWithoutGarlic.classList.remove('visible-state');
        handWithoutGarlic.classList.remove('after-move');
    }

    function clearFeedAnimationTimeouts() {
        feedAnimationTimeouts.forEach(timeoutId => clearTimeout(timeoutId));
        feedAnimationTimeouts = [];
    }

    function finishGame() {
        if (gameTimerInterval) {
            clearInterval(gameTimerInterval);
            gameTimerInterval = null;
        }

        clearFeedAnimationTimeouts();

        gamePhase = 'finished';
        currentStep = 'finished';

        playTimer.textContent = '00:00';
        playTimer.classList.remove('timer-warning');
        playTimer.classList.add('timer-danger');

        resetCharactersToIdle();

        hitCount.classList.remove('visible', 'pop');

        finishText.classList.remove('show');
        void finishText.offsetWidth;
        finishText.classList.add('show');
    }

    function wait(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
});