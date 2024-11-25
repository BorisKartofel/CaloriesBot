package telegram.Calories_Bot.data;

/**
 * При обработке запроса от пользователя, элемент отображается в соответствии с именем Callback'а
 */
public enum CallbackData {
    /**
     * Будет отображен элемент главной страницы
     */
    main,

    notification_main, notification_new, notification_edit_title_, notification_edit_descr_, notification_edit_time_,
    notification_done_, notification_back_,


    PRODUCT_MAIN, PRODUCT_SENDING_EATEN_, PRODUCT_CREATE_NEW, PRODUCT_HAS_BEEN_EATEN_, PRODUCT_RETURN_,
    PRODUCT_SENDING_GRAM_, PRODUCT_ADD,

    /**
     * Будет отображен элемент, связанный с просмотром съеденных каллорий за период
     */
    PRODUCT_SHOW_EATEN_PAST_DAYS;

}
