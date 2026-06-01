document.addEventListener('DOMContentLoaded', () => {
    const gameBox = document.getElementById('bossGameBox');

    const startScreen = document.getElementById('startScreen');
    const guideScreen = document.getElementById('guideScreen');
    const playScreen = document.getElementById('playScreen');
    const resultScreen = document.getElementById('resultScreen');

    const guideContent = document.getElementById('guideContent');

    const playTimer = document.getElementById('playTimer');
    const countdownText = document.getElementById('countdownText');
    const hitCount = document.getElementById('hitCount');
    const finishText = document.getElementById('finishText');

    const danbiNormal = document.getElementById('danbiNormal');
    const danbiMouth = document.getElementById('danbiMouth');

    const handWithGarlic = document.getElementById('handWithGarlic');
    const handWithoutGarlic = document.getElementById('handWithoutGarlic');

    const resultPlayerName = document.getElementById('resultPlayerName');
    const resultHitCount = document.getElementById('resultHitCount');
    const resultGarlicCount = document.getElementById('resultGarlicCount');
    const resultGradeText = document.getElementById('resultGradeText');

    const resultRowPlayer = document.getElementById('resultRowPlayer');
    const resultRowHit = document.getElementById('resultRowHit');
    const resultRowGarlic = document.getElementById('resultRowGarlic');
    const resultGradeStamp = document.getElementById('resultGradeStamp');
    const backButton = document.getElementById('backButton');

    let currentStep = 'start';
    let gamePhase = 'idle';

    let playerNickname = '플레이어';
    let currentGarlicCount = 0;

    let hitTotal = 0;
    let gameTimerInterval = null;
    let gameEndTime = 0;
    let feedAnimationTimeouts = [];
    let rewardSaved = false;

    const GAME_DURATION = 15000;

    initialize();

    async function initialize() {
        backButton.addEventListener('click', handleBackButtonClick);
        gameBox.addEventListener('click', handleGameBoxClick);

        await loadUserInfo();
    }

    function handleBackButtonClick(event) {
        event.stopPropagation();
        history.back();
    }

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

    async function loadUserInfo() {
        const token = getAccessToken();

        if (!token) {
            return;
        }

        try {
            const response = await fetch('/users', {
                method: 'GET',
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });

            if (!response.ok) {
                return;
            }

            const data = await response.json();

            playerNickname = data.nickname || '플레이어';
            currentGarlicCount = Number(data.garlic_count ?? 0);
        } catch (error) {
            console.error('유저 정보 조회 실패:', error);
        }
    }

    function getAccessToken() {
        return localStorage.getItem('accessToken')
            || localStorage.getItem('access_token')
            || sessionStorage.getItem('accessToken')
            || sessionStorage.getItem('access_token')
            || '';
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
        rewardSaved = false;

        hitTotal = 0;

        playTimer.textContent = '00:15';
        playTimer.classList.remove('timer-danger');

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
        resultScreen.classList.remove('active');

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

        playTimer.classList.remove('timer-danger');
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
        playTimer.classList.remove('timer-danger');

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

        danbiNormal.classList.remove('visible-state');
        danbiMouth.classList.add('visible-state');

        handWithGarlic.classList.add('visible-state');
        handWithGarlic.classList.add('hit-move');

        handWithoutGarlic.classList.remove('visible-state');
        handWithoutGarlic.classList.remove('after-move');

        const firstTimeout = setTimeout(() => {
            danbiMouth.classList.remove('visible-state');
            danbiNormal.classList.add('visible-state');

            handWithGarlic.classList.remove('visible-state');
            handWithGarlic.classList.remove('hit-move');

            handWithoutGarlic.classList.add('visible-state');
            handWithoutGarlic.classList.add('after-move');
        }, 110);

        const secondTimeout = setTimeout(() => {
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

    async function finishGame() {
        if (gamePhase === 'finished') {
            return;
        }

        if (gameTimerInterval) {
            clearInterval(gameTimerInterval);
            gameTimerInterval = null;
        }

        clearFeedAnimationTimeouts();

        gamePhase = 'finished';
        currentStep = 'finished';

        playTimer.textContent = '00:00';
        playTimer.classList.add('timer-danger');

        resetCharactersToIdle();

        hitCount.classList.remove('visible', 'pop');

        finishText.classList.remove('show');
        void finishText.offsetWidth;
        finishText.classList.add('show');

        const rewardResult = getRewardResult(hitTotal);

        if (!rewardSaved) {
            await saveRewardResult(rewardResult);
            rewardSaved = true;
        }

        await wait(1200);
        await showResultSequence(rewardResult);
    }

    function getRewardResult(totalHits) {
        if (totalHits >= 120) {
            return {
                grade: 'A',
                garlicReward: 200
            };
        }

        if (totalHits >= 90) {
            return {
                grade: 'B',
                garlicReward: 150
            };
        }

        if (totalHits >= 60) {
            return {
                grade: 'C',
                garlicReward: 110
            };
        }

        if (totalHits >= 30) {
            return {
                grade: 'D',
                garlicReward: 70
            };
        }

        return {
            grade: 'F',
            garlicReward: 30
        };
    }

    async function saveRewardResult(rewardResult) {
        const token = getAccessToken();

        if (!token) {
            currentGarlicCount += rewardResult.garlicReward;
            return;
        }

        try {
            const response = await fetch('/users/garlic', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify({
                    amount: rewardResult.garlicReward
                })
            });

            if (!response.ok) {
                currentGarlicCount += rewardResult.garlicReward;
                return;
            }

            const data = await response.json();

            playerNickname = data.nickname || playerNickname;
            currentGarlicCount = Number(data.garlic_count ?? (currentGarlicCount + rewardResult.garlicReward));
        } catch (error) {
            console.error('마늘 누적 저장 실패:', error);
            currentGarlicCount += rewardResult.garlicReward;
        }
    }

    async function showResultSequence(rewardResult) {
        activateScreen(resultScreen);

        resultPlayerName.textContent = playerNickname;
        resultHitCount.textContent = `+ ${hitTotal}`;
        resultGarlicCount.textContent = `${rewardResult.garlicReward}개`;
        resultGradeText.textContent = rewardResult.grade;

        resetResultSequenceUI();

        await wait(450);
        resultRowPlayer.classList.add('show');

        await wait(750);
        resultRowHit.classList.add('show');

        await wait(750);
        resultRowGarlic.classList.add('show');

        await wait(850);
        resultGradeStamp.classList.add('show');

        await wait(700);
        backButton.classList.add('show');
    }

    function resetResultSequenceUI() {
        resultRowPlayer.classList.remove('show');
        resultRowHit.classList.remove('show');
        resultRowGarlic.classList.remove('show');
        resultGradeStamp.classList.remove('show');
        backButton.classList.remove('show');
    }

    function wait(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
});