package com.hackathon.finni.features.tasks

import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.task_robot_title
import com.hackathon.finni.resources.task_robot_story
import com.hackathon.finni.resources.task_robot_repair
import com.hackathon.finni.resources.task_robot_wait
import com.hackathon.finni.resources.task_robot_repair_feedback
import com.hackathon.finni.resources.task_robot_wait_feedback

data class DilemmaChoice(
    val id: String,
    val title: UiText,
    val feedback: UiText,
    val effect: DilemmaEffect
)

data class DilemmaTask(
    val id: String,
    val title: UiText,
    val story: UiText,
    val choices: List<DilemmaChoice>
)

/** Content is separate from the renderer; further dilemmas use the same contract. */
object DilemmaCatalog {
    const val ROBOT_ID = "broken_robot"

    val brokenRobot = DilemmaTask(
        id = ROBOT_ID,
        title = UiText.Resource(Res.string.task_robot_title),
        story = UiText.Resource(Res.string.task_robot_story),
        choices = listOf(
            DilemmaChoice(
                id = "repair",
                title = UiText.Resource(Res.string.task_robot_repair),
                feedback = UiText.Resource(Res.string.task_robot_repair_feedback),
                effect = DilemmaEffect(20, -20, "Neutral", false)
            ),
            DilemmaChoice(
                id = "wait",
                title = UiText.Resource(Res.string.task_robot_wait),
                feedback = UiText.Resource(Res.string.task_robot_wait_feedback),
                effect = DilemmaEffect(20, 0, "Happy", true)
            )
        )
    )

    private fun task(id: String, title: String, story: String, choices: List<DilemmaChoice>) = DilemmaTask(
        id = id, title = UiText.Dynamic(title), story = UiText.Dynamic(story), choices = choices
    )

    private fun choice(id: String, title: String, feedback: String, effect: DilemmaEffect) = DilemmaChoice(
        id = id, title = UiText.Dynamic(title), feedback = UiText.Dynamic(feedback), effect = effect
    )

    val firstEnvelopes = task(
        "first_envelopes", "Первые конверты",
        "У Финни 120 монет. Распредели их так, чтобы остались деньги на еду, мечту и запас.",
        listOf(
            choice("plan", "Еда 40, копилка 50, резерв 30", "Отличный план: есть деньги на нужное, мечту и неожиданности.", DilemmaEffect(30, 50, "Happy", true)),
            choice("all_savings", "Положить все 120 в копилку", "Копилка выросла, но на еду и запас ничего не осталось. План лучше делать с местом для нужного.", DilemmaEffect(30, 120, "Neutral", false))
        )
    )

    val honestWork = task(
        "honest_work", "Честный труд",
        "На доске объявлений есть разные способы получить монеты. Выбери безопасный и честный.",
        listOf(
            choice("help", "Помочь соседу выгулять собаку", "Помощь — честный способ заработать. Финни получил награду за полезное дело.", DilemmaEffect(30, 0, "Happy", true)),
            choice("link", "Перейти по ссылке за 1000 монет", "Обещания лёгких денег могут быть ловушкой. Не переходи по подозрительным ссылкам.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val shoppingList = task(
        "shopping_list", "Дисциплина списка",
        "В списке покупок только вода за 10 монет. У кассы лежит яркий леденец.",
        listOf(
            choice("water", "Купить воду по списку", "Список помог не потратить лишнее. Сохранённые монеты пригодятся для мечты.", DilemmaEffect(15, 0, "Happy", true)),
            choice("candy", "Взять воду и леденец", "Леденец не был в плане. В следующий раз сначала проверь список и остаток бюджета.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val ruler = task(
        "reliable_ruler", "Баланс цены и пользы",
        "Финни нужна линейка: хлипкая за 5, надёжная за 15 или с играми за 45 монет.",
        listOf(
            choice("reliable", "Выбрать надёжную линейку за 15", "Надёжная вещь прослужит долго и не заставит тратиться снова.", DilemmaEffect(20, 0, "Happy", true)),
            choice("cheap", "Взять хлипкую линейку за 5", "Слишком дешёвая линейка быстро сломается. Цена важна вместе с качеством.", DilemmaEffect(0, 0, "Neutral", false)),
            choice("games", "Взять линейку с играми за 45", "Лишние функции делают покупку дороже, но не помогают на уроке.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val notebook = task(
        "check_notebook", "Внимание у прилавка",
        "На полке две тетради: уценённая за 8 и обычная за 10 монет. Одна из них с браком.",
        listOf(
            choice("inspect", "Осмотреть и купить обычную тетрадь", "Ты проверил товар до покупки и выбрал тетрадь без брака.", DilemmaEffect(20, 0, "Happy", true)),
            choice("discount", "Купить уценённую тетрадь не глядя", "У обложки дефект, а страницы выпадают. Перед покупкой полезно осматривать товар.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val lunch = task(
        "healthy_lunch", "На чём нельзя экономить",
        "Финни хочет быстрее накопить на телескоп и предлагает пропустить обед за 20 монет.",
        listOf(
            choice("lunch", "Пообедать за 20 монет", "Еда и здоровье — важные расходы. Финни полон сил для дальнейших дел.", DilemmaEffect(20, 0, "Happy", true)),
            choice("skip", "Пропустить обед и положить 20 в копилку", "Экономить на еде нельзя: Финни остался без сил. Сначала позаботься о нужном.", DilemmaEffect(0, 20, "Sad", false))
        )
    )

    val friendGift = task(
        "friend_gift", "День рождения друга",
        "Друг празднует день рождения. Есть дорогой робот за 70 монет и идея нарисовать комикс фломастерами за 5.",
        listOf(
            choice("comic", "Купить фломастеры и нарисовать комикс", "Внимание и творчество сделали подарок особенным, а копилка осталась в безопасности.", DilemmaEffect(20, 0, "Happy", true)),
            choice("robot", "Взять 70 монет из копилки на робота", "Подарок куплен, но мечта отдалилась. Необязательные покупки лучше оплачивать свободными деньгами.", DilemmaEffect(20, -70, "Neutral", false))
        )
    )

    val healthySnack = task(
        "healthy_snack", "Полезный перекус",
        "После прогулки Финни проголодался. В магазине есть яблоко за 10 монет и большая пачка чипсов за 10.",
        listOf(
            choice("apple", "Взять яблоко", "Яблоко утоляет голод и помогает Финни сохранить силы.", DilemmaEffect(15, 0, "Happy", true)),
            choice("chips", "Взять чипсы", "Чипсы кажутся вкусными, но не заменяют полезный перекус.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val tvAd = task(
        "tv_ad", "Сказки из телевизора",
        "Реклама обещает, что светящийся браслет за 60 монет сделает любую домашнюю работу лёгкой.",
        listOf(
            choice("check", "Проверить отзывы и не спешить с покупкой", "Ты не поверил громкому обещанию и сначала решил узнать, чем браслет полезен на самом деле.", DilemmaEffect(15, 0, "Happy", true)),
            choice("buy", "Купить браслет сразу", "Яркая реклама не гарантирует пользу. Перед крупной покупкой стоит собрать информацию.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val vendingMachine = task(
        "vending_machine", "Коварный автомат",
        "Игровой автомат предлагает попытать удачу за 20 монет: приз может быть, а может и не быть.",
        listOf(
            choice("save", "Оставить монеты на запланированную цель", "Ты сохранил деньги для того, что действительно важно, и не стал рисковать ими случайно.", DilemmaEffect(20, 0, "Happy", true)),
            choice("play", "Сыграть несколько раз", "В автомате нельзя заранее узнать результат. Случайная игра может незаметно забрать весь бюджет.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val falseDeal = task(
        "false_deal", "Ложная выгода",
        "Магазин предлагает три одинаковых наклейки по акции «1+1=3». Финни нужна только одна наклейка за 12 монет.",
        listOf(
            choice("one", "Купить одну нужную наклейку", "Ты сравнил предложение со своим списком и не заплатил за лишнее.", DilemmaEffect(15, 0, "Happy", true)),
            choice("three", "Взять три наклейки ради акции", "Скидка полезна только тогда, когда все вещи действительно нужны.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val backpack = task(
        "backpack", "Испытание на прочность",
        "У Финни порвался школьный рюкзак. Можно купить прочный за 45 монет или потратить эти деньги на новую игру и ходить со старым.",
        listOf(
            choice("backpack", "Купить прочный рюкзак", "Ты поставил важную вещь выше развлечения и подготовился к школе.", DilemmaEffect(20, 0, "Happy", true)),
            choice("game", "Купить игру и отложить рюкзак", "Игра приятна, но рюкзак нужен каждый день. Сначала лучше закрывать важные расходы.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val gardenWork = task(
        "garden_work", "Честная подработка",
        "Соседка просит помочь собрать листья в саду и предлагает 30 монет. Друг зовёт взять деньги из её кошелька без спроса.",
        listOf(
            choice("help", "Помочь в саду и заработать честно", "Ты выполнил полезное дело и честно получил награду за свой труд.", DilemmaEffect(30, 0, "Happy", true)),
            choice("take", "Взять деньги без разрешения", "Чужие деньги нельзя брать без спроса. Доход должен быть честным.", DilemmaEffect(0, 0, "Sad", false))
        )
    )

    val grandmotherGift = task(
        "grandmother_gift", "Нежданный подарок",
        "Бабушка подарила Финни 100 монет. Телескоп всё ещё требует накоплений, а в витрине лежит дорогая игрушка.",
        listOf(
            choice("save", "Часть подарка положить в копилку", "Ты приблизил большую цель и всё равно можешь оставить немного денег на маленькую радость.", DilemmaEffect(15, 50, "Happy", true)),
            choice("toy", "Потратить весь подарок на игрушку", "Покупка радует сейчас, но запас и мечта остались без пополнения.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val fairAudit = task(
        "fair_audit", "Финальная ревизия",
        "Перед школьной ярмаркой Финни считает деньги. На нужные материалы уйдёт 40 монет, а в кошельке есть 70.",
        listOf(
            choice("plan", "Записать расходы и оставить 30 монет запасом", "Ты составил простой план и сохранил запас на непредвиденный случай.", DilemmaEffect(20, 0, "Happy", true)),
            choice("spend", "Купить материалы и сувениры на все 70", "Без запаса любая неожиданность может сорвать планы. Бюджету нужна свободная часть.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val fairAccounts = task(
        "fair_accounts", "Сверка счетов и триумф",
        "На ярмарке Финни заработал 80 монет. До телескопа не хватает 60, но хочется сразу купить сладости за 25.",
        listOf(
            choice("telescope", "Сначала отложить 60 на телескоп", "Ты достиг цели, потому что сверил доход с планом до необязательных трат.", DilemmaEffect(20, 60, "Happy", true)),
            choice("sweets", "Сначала купить сладости", "Маленькая покупка отодвигает цель. Лучше сначала выполнить важный план.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val newHorizons = task(
        "new_horizons", "Новые горизонты",
        "Телескоп уже куплен. Финни думает, что делать с новыми 40 монетами: потратить всё сегодня или выбрать следующую цель.",
        listOf(
            choice("goal", "Выбрать новую цель и отложить часть денег", "После достижения одной мечты можно спокойно планировать следующую.", DilemmaEffect(20, 20, "Happy", true)),
            choice("spend", "Потратить всё без плана", "Отдых приятен, но новый план помогает деньгам работать на будущую мечту.", DilemmaEffect(0, 0, "Neutral", false))
        )
    )

    val all = listOf(
        brokenRobot, firstEnvelopes, honestWork, shoppingList, ruler, notebook, lunch, friendGift,
        healthySnack, tvAd, vendingMachine, falseDeal, backpack, gardenWork, grandmotherGift,
        fairAudit, fairAccounts, newHorizons
    )

    fun byId(id: String): DilemmaTask? = all.find { it.id == id }
}
