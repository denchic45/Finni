package com.hackathon.finni.features.levels.model

object LevelsData {
    val defaultLevels: List<LevelItem> = listOf(
        // Chapter 1: Новая комната
        LevelItem(
            id = 1,
            number = 1,
            title = "Первые конверты",
            description = "Знакомство с правилом 3-х конвертов",
            chapterNumber = 1,
            chapterTitle = "Новая комната",
            status = LevelStatus.COMPLETED,
            stars = 3,
            rewardCoins = 30
        ),
        LevelItem(
            id = 2,
            number = 2,
            title = "Честный труд",
            description = "Источники дохода: честно или обман",
            chapterNumber = 1,
            chapterTitle = "Новая комната",
            status = LevelStatus.COMPLETED,
            stars = 3,
            rewardCoins = 30
        ),
        LevelItem(
            id = 3,
            number = 3,
            title = "Первая дилемма",
            description = "Хотелка против мечты: сломанный робот",
            chapterNumber = 1,
            chapterTitle = "Новая комната",
            status = LevelStatus.COMPLETED,
            stars = 3,
            rewardCoins = 20
        ),

        // Chapter 2: Умный покупатель
        LevelItem(
            id = 4,
            number = 4,
            title = "Дисциплина списка",
            description = "Маркетинг у кассы и список покупок",
            chapterNumber = 2,
            chapterTitle = "Умный покупатель",
            status = LevelStatus.COMPLETED,
            stars = 3,
            rewardCoins = 25
        ),
        LevelItem(
            id = 5,
            number = 5,
            title = "Баланс цены и пользы",
            description = "Осмотр свойств и выбор линейки",
            chapterNumber = 2,
            chapterTitle = "Умный покупатель",
            status = LevelStatus.COMPLETED,
            stars = 3,
            rewardCoins = 25
        ),
        LevelItem(
            id = 6,
            number = 6,
            title = "Внимание у прилавка",
            description = "Проверка качества до оплаты",
            chapterNumber = 2,
            chapterTitle = "Умный покупатель",
            status = LevelStatus.COMPLETED,
            stars = 3,
            rewardCoins = 25
        ),

        // Chapter 3: Друзья и здоровье
        LevelItem(
            id = 7,
            number = 7,
            title = "На чем нельзя экономить",
            description = "Ложная экономия: обед против здоровья",
            chapterNumber = 3,
            chapterTitle = "Друзья и здоровье",
            status = LevelStatus.CURRENT,
            stars = 0,
            rewardCoins = 25
        ),
        LevelItem(
            id = 8,
            number = 8,
            title = "Полезный перекус",
            description = "Выбор между яблоком и чипсами",
            chapterNumber = 3,
            chapterTitle = "Друзья и здоровье",
            status = LevelStatus.LOCKED,
            stars = 0,
            rewardCoins = 20
        ),
        LevelItem(
            id = 9,
            number = 9,
            title = "День рождения друга",
            description = "Подарок своими руками или траты",
            chapterNumber = 3,
            chapterTitle = "Друзья и здоровье",
            status = LevelStatus.LOCKED,
            stars = 0,
            rewardCoins = 25
        ),

        // Chapter 4: Яркие ловушки
        LevelItem(
            id = 10,
            number = 10,
            title = "Сказки из телевизора",
            description = "Анализ рекламных обещаний",
            chapterNumber = 4,
            chapterTitle = "Яркие ловушки",
            status = LevelStatus.LOCKED,
            stars = 0,
            rewardCoins = 25
        ),
        LevelItem(
            id = 11,
            number = 11,
            title = "Коварный автомат",
            description = "Цена азарта и правила вероятности",
            chapterNumber = 4,
            chapterTitle = "Яркие ловушки",
            status = LevelStatus.LOCKED,
            stars = 0,
            rewardCoins = 25
        ),
        LevelItem(
            id = 12,
            number = 12,
            title = "Ложная выгода",
            description = "Акции «1+1=3» и мнимая скидка",
            chapterNumber = 4,
            chapterTitle = "Яркие ловушки",
            status = LevelStatus.LOCKED,
            stars = 0,
            rewardCoins = 25
        ),

        // Chapter 5: Планы меняются
        LevelItem(
            id = 13,
            number = 13,
            title = "Испытание на прочность",
            description = "Форс-мажор: порвался школьный рюкзак",
            chapterNumber = 5,
            chapterTitle = "Планы меняются",
            status = LevelStatus.LOCKED,
            stars = 0,
            rewardCoins = 30
        ),
        LevelItem(
            id = 14,
            number = 14,
            title = "Честная подработка",
            description = "Помощь в саду и заработок",
            chapterNumber = 5,
            chapterTitle = "Планы меняются",
            status = LevelStatus.LOCKED,
            stars = 0,
            rewardCoins = 30
        ),
        LevelItem(
            id = 15,
            number = 15,
            title = "Нежданный подарок",
            description = "Подарок от бабушки и подушка безопасности",
            chapterNumber = 5,
            chapterTitle = "Планы меняются",
            status = LevelStatus.LOCKED,
            stars = 0,
            rewardCoins = 40
        ),

        // Chapter 6: Большая ярмарка
        LevelItem(
            id = 16,
            number = 16,
            title = "Финальная ревизия",
            description = "Подготовка бюджета к ярмарке",
            chapterNumber = 6,
            chapterTitle = "Большая ярмарка",
            status = LevelStatus.LOCKED,
            stars = 0,
            rewardCoins = 35
        ),
        LevelItem(
            id = 17,
            number = 17,
            title = "Сверка счетов и триумф",
            description = "Покупка цели мечты (Телескоп)",
            chapterNumber = 6,
            chapterTitle = "Большая ярмарка",
            status = LevelStatus.LOCKED,
            stars = 0,
            rewardCoins = 50
        ),
        LevelItem(
            id = 18,
            number = 18,
            title = "Новые горизонты",
            description = "Финал сезона и постановка новой цели",
            chapterNumber = 6,
            chapterTitle = "Большая ярмарка",
            status = LevelStatus.LOCKED,
            stars = 0,
            rewardCoins = 50
        )
    )
}
