# Genera app/src/main/res/values*/strings.xml. Eseguire: python3 tools_gen_strings.py
import os
from xml.sax.saxutils import escape

KEYS = """app_name home_no_contacts home_keypad home_call_in_progress home_setup_needed call_no_permission call_failed
call_incoming call_answer call_hangup call_dialing call_on_hold call_ended call_hidden notif_channel_calls keypad_call
keypad_delete back settings_title pin_enter pin_wrong pin_new pin_confirm pin_mismatch pin_too_short section_setup
setup_dialer setup_home setup_permissions status_ok status_todo action_activate action_change about_speaker
section_contacts contact_add contact_edit contact_name contact_number contact_photo_choose contact_photo_remove
contact_import contact_delete_confirm delete cancel save move_up move_down section_calls pref_show_keypad filter_title
filter_off filter_blacklist filter_whitelist pref_block_hidden blacklist_title whitelist_title number_add list_empty
section_security pin_set pin_change pin_remove pin_status_on pin_status_off section_language language_system
section_other open_home_chooser open_android_settings""".split()

T = {}
T[""] = ["Simple Phone",
"No contacts yet.\nA family member can add them from the settings (small icon at the bottom right).",
"Keypad", "Call in progress – tap to return", "Setup incomplete – open settings",
"Permission to make calls is missing", "Unable to make the call",
"Is calling you", "Answer", "Hang up", "Calling…", "On hold", "Call ended", "Private number", "Incoming calls",
"Call", "Delete", "Back", "Settings", "Enter the code", "Wrong code", "New code (4–8 digits)", "Repeat the code",
"The codes do not match", "The code must have 4 to 8 digits", "Setup",
"Default phone app", "Home screen", "Permissions", "Active ✓", "Not active", "Activate", "Change",
"Calls are always made and received on speakerphone.",
"Contacts", "Add contact", "Edit contact", "Name", "Phone number", "Choose photo", "Remove photo",
"Pick from address book", "Delete %1$s?", "Delete", "Cancel", "Save", "Move up", "Move down",
"Calls", "Show the Keypad button on the home screen", "Incoming calls",
"Accept all calls", "Block numbers in the blacklist", "Accept only contacts and whitelist",
"Block private/hidden numbers", "Blacklist", "Whitelist (in addition to contacts)", "Add number", "No numbers",
"Security", "Set code", "Change code", "Remove code", "Settings are protected by a code",
"No code: anyone can open the settings", "Language", "System language",
"Other", "Change home app (to exit)", "Open Android settings"]
T["it"] = ["Telefono Semplice",
"Nessun contatto.\nUn familiare può aggiungerli dalle impostazioni (iconcina in basso a destra).",
"Tastiera", "Chiamata in corso – tocca per tornare", "Configurazione incompleta – apri le impostazioni",
"Manca il permesso per telefonare", "Impossibile effettuare la chiamata",
"Ti sta chiamando", "Rispondi", "Butta giù", "Sto chiamando…", "In attesa", "Chiamata terminata", "Numero privato", "Chiamate in arrivo",
"Chiama", "Cancella", "Indietro", "Impostazioni", "Inserisci il codice", "Codice errato", "Nuovo codice (4–8 cifre)", "Ripeti il codice",
"I codici non coincidono", "Il codice deve avere da 4 a 8 cifre", "Configurazione",
"App Telefono predefinita", "Schermata Home", "Permessi", "Attivo ✓", "Non attivo", "Attiva", "Cambia",
"Le chiamate si fanno e si ricevono sempre in vivavoce.",
"Contatti", "Aggiungi contatto", "Modifica contatto", "Nome", "Numero di telefono", "Scegli foto", "Togli foto",
"Prendi dalla rubrica", "Eliminare %1$s?", "Elimina", "Annulla", "Salva", "Sposta su", "Sposta giù",
"Chiamate", "Mostra il tasto Tastiera nella Home", "Chiamate in arrivo",
"Accetta tutte le chiamate", "Blocca i numeri della lista nera", "Accetta solo contatti e lista bianca",
"Blocca numeri privati/nascosti", "Lista nera", "Lista bianca (oltre ai contatti)", "Aggiungi numero", "Nessun numero",
"Sicurezza", "Imposta codice", "Cambia codice", "Rimuovi codice", "Le impostazioni sono protette da codice",
"Nessun codice: chiunque può aprire le impostazioni", "Lingua", "Lingua del sistema",
"Altro", "Cambia app Home (per uscire)", "Apri impostazioni di Android"]
T["es"] = ["Teléfono Sencillo",
"Todavía no hay contactos.\nUn familiar puede añadirlos desde los ajustes (icono pequeño abajo a la derecha).",
"Teclado", "Llamada en curso – toca para volver", "Configuración incompleta – abre los ajustes",
"Falta el permiso para llamar", "No se puede realizar la llamada",
"Te está llamando", "Contestar", "Colgar", "Llamando…", "En espera", "Llamada finalizada", "Número privado", "Llamadas entrantes",
"Llamar", "Borrar", "Atrás", "Ajustes", "Introduce el código", "Código incorrecto", "Nuevo código (4–8 cifras)", "Repite el código",
"Los códigos no coinciden", "El código debe tener de 4 a 8 cifras", "Configuración",
"App de teléfono predeterminada", "Pantalla de inicio", "Permisos", "Activo ✓", "No activo", "Activar", "Cambiar",
"Las llamadas siempre se hacen y se reciben en manos libres.",
"Contactos", "Añadir contacto", "Editar contacto", "Nombre", "Número de teléfono", "Elegir foto", "Quitar foto",
"Elegir de la agenda", "¿Eliminar a %1$s?", "Eliminar", "Cancelar", "Guardar", "Subir", "Bajar",
"Llamadas", "Mostrar el botón Teclado en el inicio", "Llamadas entrantes",
"Aceptar todas las llamadas", "Bloquear los números de la lista negra", "Aceptar solo contactos y lista blanca",
"Bloquear números privados/ocultos", "Lista negra", "Lista blanca (además de los contactos)", "Añadir número", "Ningún número",
"Seguridad", "Establecer código", "Cambiar código", "Quitar código", "Los ajustes están protegidos con un código",
"Sin código: cualquiera puede abrir los ajustes", "Idioma", "Idioma del sistema",
"Otros", "Cambiar app de inicio (para salir)", "Abrir ajustes de Android"]
T["fr"] = ["Téléphone Simple",
"Aucun contact pour l'instant.\nUn proche peut les ajouter depuis les réglages (petite icône en bas à droite).",
"Clavier", "Appel en cours – touchez pour revenir", "Configuration incomplète – ouvrez les réglages",
"Autorisation d'appeler manquante", "Impossible de passer l'appel",
"Vous appelle", "Répondre", "Raccrocher", "Appel en cours…", "En attente", "Appel terminé", "Numéro masqué", "Appels entrants",
"Appeler", "Effacer", "Retour", "Réglages", "Saisissez le code", "Code erroné", "Nouveau code (4 à 8 chiffres)", "Répétez le code",
"Les codes ne correspondent pas", "Le code doit comporter 4 à 8 chiffres", "Configuration",
"Application Téléphone par défaut", "Écran d'accueil", "Autorisations", "Actif ✓", "Inactif", "Activer", "Modifier",
"Les appels sont toujours passés et reçus en haut-parleur.",
"Contacts", "Ajouter un contact", "Modifier le contact", "Nom", "Numéro de téléphone", "Choisir une photo", "Retirer la photo",
"Choisir dans le répertoire", "Supprimer %1$s ?", "Supprimer", "Annuler", "Enregistrer", "Monter", "Descendre",
"Appels", "Afficher le bouton Clavier sur l'accueil", "Appels entrants",
"Accepter tous les appels", "Bloquer les numéros de la liste noire", "Accepter seulement les contacts et la liste blanche",
"Bloquer les numéros masqués", "Liste noire", "Liste blanche (en plus des contacts)", "Ajouter un numéro", "Aucun numéro",
"Sécurité", "Définir un code", "Changer le code", "Supprimer le code", "Les réglages sont protégés par un code",
"Pas de code : tout le monde peut ouvrir les réglages", "Langue", "Langue du système",
"Autre", "Changer d'application d'accueil (pour quitter)", "Ouvrir les réglages Android"]
T["de"] = ["Einfaches Telefon",
"Noch keine Kontakte.\nEin Angehöriger kann sie in den Einstellungen hinzufügen (kleines Symbol unten rechts).",
"Tastatur", "Anruf läuft – tippen zum Zurückkehren", "Einrichtung unvollständig – Einstellungen öffnen",
"Berechtigung zum Telefonieren fehlt", "Anruf nicht möglich",
"Ruft dich an", "Annehmen", "Auflegen", "Wird angerufen…", "Gehalten", "Anruf beendet", "Unbekannte Nummer", "Eingehende Anrufe",
"Anrufen", "Löschen", "Zurück", "Einstellungen", "Code eingeben", "Falscher Code", "Neuer Code (4–8 Ziffern)", "Code wiederholen",
"Die Codes stimmen nicht überein", "Der Code muss 4 bis 8 Ziffern haben", "Einrichtung",
"Standard-Telefon-App", "Startbildschirm", "Berechtigungen", "Aktiv ✓", "Nicht aktiv", "Aktivieren", "Ändern",
"Anrufe laufen immer über den Lautsprecher.",
"Kontakte", "Kontakt hinzufügen", "Kontakt bearbeiten", "Name", "Telefonnummer", "Foto wählen", "Foto entfernen",
"Aus dem Adressbuch wählen", "%1$s löschen?", "Löschen", "Abbrechen", "Speichern", "Nach oben", "Nach unten",
"Anrufe", "Tastatur-Taste auf dem Startbildschirm zeigen", "Eingehende Anrufe",
"Alle Anrufe annehmen", "Nummern der Sperrliste blockieren", "Nur Kontakte und Erlaubt-Liste annehmen",
"Unterdrückte Nummern blockieren", "Sperrliste", "Erlaubt-Liste (zusätzlich zu den Kontakten)", "Nummer hinzufügen", "Keine Nummern",
"Sicherheit", "Code festlegen", "Code ändern", "Code entfernen", "Die Einstellungen sind durch einen Code geschützt",
"Kein Code: jeder kann die Einstellungen öffnen", "Sprache", "Systemsprache",
"Sonstiges", "Start-App wechseln (zum Verlassen)", "Android-Einstellungen öffnen"]
T["pt"] = ["Telefone Simples",
"Ainda não há contactos.\nUm familiar pode adicioná-los nas definições (ícone pequeno no canto inferior direito).",
"Teclado", "Chamada em curso – toque para voltar", "Configuração incompleta – abra as definições",
"Falta a permissão para telefonar", "Não foi possível fazer a chamada",
"Está a ligar-lhe", "Atender", "Desligar", "A ligar…", "Em espera", "Chamada terminada", "Número privado", "Chamadas recebidas",
"Ligar", "Apagar", "Voltar", "Definições", "Introduza o código", "Código errado", "Novo código (4–8 dígitos)", "Repita o código",
"Os códigos não coincidem", "O código deve ter de 4 a 8 dígitos", "Configuração",
"App de telefone predefinida", "Ecrã inicial", "Permissões", "Ativo ✓", "Inativo", "Ativar", "Alterar",
"As chamadas são sempre feitas e recebidas em alta voz.",
"Contactos", "Adicionar contacto", "Editar contacto", "Nome", "Número de telefone", "Escolher foto", "Remover foto",
"Escolher da agenda", "Eliminar %1$s?", "Eliminar", "Cancelar", "Guardar", "Mover para cima", "Mover para baixo",
"Chamadas", "Mostrar o botão Teclado no ecrã inicial", "Chamadas recebidas",
"Aceitar todas as chamadas", "Bloquear os números da lista negra", "Aceitar só contactos e lista branca",
"Bloquear números privados/ocultos", "Lista negra", "Lista branca (além dos contactos)", "Adicionar número", "Nenhum número",
"Segurança", "Definir código", "Alterar código", "Remover código", "As definições estão protegidas por código",
"Sem código: qualquer pessoa pode abrir as definições", "Idioma", "Idioma do sistema",
"Outros", "Mudar app inicial (para sair)", "Abrir definições do Android"]
T["ru"] = ["Простой телефон",
"Контактов пока нет.\nРодственник может добавить их в настройках (маленький значок внизу справа).",
"Клавиатура", "Идёт звонок – нажмите, чтобы вернуться", "Настройка не завершена – откройте настройки",
"Нет разрешения на звонки", "Не удалось позвонить",
"Вам звонит", "Ответить", "Положить трубку", "Звоню…", "На удержании", "Звонок завершён", "Скрытый номер", "Входящие звонки",
"Позвонить", "Стереть", "Назад", "Настройки", "Введите код", "Неверный код", "Новый код (4–8 цифр)", "Повторите код",
"Коды не совпадают", "Код должен содержать от 4 до 8 цифр", "Настройка",
"Приложение «Телефон» по умолчанию", "Главный экран", "Разрешения", "Включено ✓", "Не включено", "Включить", "Изменить",
"Звонки всегда идут через громкую связь.",
"Контакты", "Добавить контакт", "Изменить контакт", "Имя", "Номер телефона", "Выбрать фото", "Убрать фото",
"Выбрать из телефонной книги", "Удалить %1$s?", "Удалить", "Отмена", "Сохранить", "Вверх", "Вниз",
"Звонки", "Показывать кнопку «Клавиатура» на главном экране", "Входящие звонки",
"Принимать все звонки", "Блокировать номера из чёрного списка", "Принимать только контакты и белый список",
"Блокировать скрытые номера", "Чёрный список", "Белый список (кроме контактов)", "Добавить номер", "Нет номеров",
"Безопасность", "Задать код", "Изменить код", "Удалить код", "Настройки защищены кодом",
"Кода нет: настройки может открыть кто угодно", "Язык", "Язык системы",
"Прочее", "Сменить главный экран (чтобы выйти)", "Открыть настройки Android"]
T["ar"] = ["هاتف بسيط",
"لا توجد جهات اتصال بعد.\nيمكن لأحد أفراد العائلة إضافتها من الإعدادات (الأيقونة الصغيرة أسفل الشاشة).",
"لوحة الأرقام", "مكالمة جارية – المس للعودة", "الإعداد غير مكتمل – افتح الإعدادات",
"إذن إجراء المكالمات غير متوفر", "تعذّر إجراء المكالمة",
"يتصل بك", "رد", "إنهاء", "جارٍ الاتصال…", "قيد الانتظار", "انتهت المكالمة", "رقم خاص", "المكالمات الواردة",
"اتصال", "حذف", "رجوع", "الإعدادات", "أدخل الرمز", "رمز خاطئ", "رمز جديد (4–8 أرقام)", "أعد إدخال الرمز",
"الرمزان غير متطابقين", "يجب أن يتكوّن الرمز من 4 إلى 8 أرقام", "الإعداد",
"تطبيق الهاتف الافتراضي", "الشاشة الرئيسية", "الأذونات", "مفعّل ✓", "غير مفعّل", "تفعيل", "تغيير",
"تتم المكالمات دائمًا عبر مكبّر الصوت.",
"جهات الاتصال", "إضافة جهة اتصال", "تعديل جهة الاتصال", "الاسم", "رقم الهاتف", "اختيار صورة", "إزالة الصورة",
"اختيار من دفتر العناوين", "حذف %1$s؟", "حذف", "إلغاء", "حفظ", "نقل لأعلى", "نقل لأسفل",
"المكالمات", "إظهار زر لوحة الأرقام في الشاشة الرئيسية", "المكالمات الواردة",
"قبول كل المكالمات", "حظر أرقام القائمة السوداء", "قبول جهات الاتصال والقائمة البيضاء فقط",
"حظر الأرقام الخاصة/المخفية", "القائمة السوداء", "القائمة البيضاء (بالإضافة إلى جهات الاتصال)", "إضافة رقم", "لا توجد أرقام",
"الأمان", "تعيين رمز", "تغيير الرمز", "إزالة الرمز", "الإعدادات محمية برمز",
"لا يوجد رمز: يمكن لأي شخص فتح الإعدادات", "اللغة", "لغة النظام",
"أخرى", "تغيير تطبيق الشاشة الرئيسية (للخروج)", "فتح إعدادات أندرويد"]
T["hi"] = ["सरल फ़ोन",
"अभी कोई संपर्क नहीं है।\nपरिवार का कोई सदस्य इन्हें सेटिंग से जोड़ सकता है (नीचे दाईं ओर छोटा आइकन)।",
"कीपैड", "कॉल चल रही है – वापस जाने के लिए छुएँ", "सेटअप अधूरा है – सेटिंग खोलें",
"कॉल करने की अनुमति नहीं है", "कॉल नहीं हो सकी",
"आपको कॉल कर रहे हैं", "उठाएँ", "काटें", "कॉल हो रही है…", "होल्ड पर", "कॉल समाप्त", "निजी नंबर", "आने वाली कॉल",
"कॉल करें", "मिटाएँ", "वापस", "सेटिंग", "कोड डालें", "गलत कोड", "नया कोड (4–8 अंक)", "कोड दोबारा डालें",
"कोड मेल नहीं खाते", "कोड में 4 से 8 अंक होने चाहिए", "सेटअप",
"डिफ़ॉल्ट फ़ोन ऐप", "होम स्क्रीन", "अनुमतियाँ", "चालू ✓", "चालू नहीं", "चालू करें", "बदलें",
"कॉल हमेशा स्पीकरफ़ोन पर होती हैं।",
"संपर्क", "संपर्क जोड़ें", "संपर्क बदलें", "नाम", "फ़ोन नंबर", "फ़ोटो चुनें", "फ़ोटो हटाएँ",
"फ़ोन बुक से चुनें", "%1$s को हटाएँ?", "हटाएँ", "रद्द करें", "सहेजें", "ऊपर ले जाएँ", "नीचे ले जाएँ",
"कॉल", "होम स्क्रीन पर कीपैड बटन दिखाएँ", "आने वाली कॉल",
"सभी कॉल स्वीकार करें", "ब्लैकलिस्ट के नंबर ब्लॉक करें", "केवल संपर्क और व्हाइटलिस्ट स्वीकार करें",
"निजी/छिपे नंबर ब्लॉक करें", "ब्लैकलिस्ट", "व्हाइटलिस्ट (संपर्कों के अलावा)", "नंबर जोड़ें", "कोई नंबर नहीं",
"सुरक्षा", "कोड सेट करें", "कोड बदलें", "कोड हटाएँ", "सेटिंग कोड से सुरक्षित हैं",
"कोई कोड नहीं: कोई भी सेटिंग खोल सकता है", "भाषा", "सिस्टम की भाषा",
"अन्य", "होम ऐप बदलें (बाहर निकलने के लिए)", "Android सेटिंग खोलें"]
T["zh"] = ["简易电话",
"还没有联系人。\n家人可以在设置中添加（右下角的小图标）。",
"拨号盘", "通话中 – 点按返回", "设置未完成 – 请打开设置",
"缺少拨打电话的权限", "无法拨打电话",
"来电", "接听", "挂断", "正在呼叫…", "保持中", "通话结束", "私密号码", "来电",
"拨打", "删除", "返回", "设置", "请输入密码", "密码错误", "新密码（4–8 位数字）", "再次输入密码",
"两次密码不一致", "密码必须为 4 到 8 位数字", "初始设置",
"默认电话应用", "主屏幕", "权限", "已启用 ✓", "未启用", "启用", "更改",
"通话始终使用免提。",
"联系人", "添加联系人", "编辑联系人", "姓名", "电话号码", "选择照片", "移除照片",
"从通讯录选择", "删除 %1$s？", "删除", "取消", "保存", "上移", "下移",
"通话", "在主屏幕显示拨号盘按钮", "来电",
"接听所有来电", "拦截黑名单中的号码", "仅接听联系人和白名单",
"拦截私密/隐藏号码", "黑名单", "白名单（联系人之外）", "添加号码", "没有号码",
"安全", "设置密码", "更改密码", "移除密码", "设置已受密码保护",
"没有密码：任何人都可以打开设置", "语言", "系统语言",
"其他", "更改主屏幕应用（用于退出）", "打开 Android 设置"]

# --- v0.2: registro chiamate, SMS, occupato
KEYS += """section_log log_missed log_rejected log_blocked log_busy log_empty log_clear log_call_back
section_sms sms_open_app sms_show_here sms_empty sms_unavailable busy_info busy_disable_waiting""".split()
T[""] += ["Missed and blocked calls", "Missed", "Hung up", "Blocked", "Busy (already on the phone)",
"No missed calls", "Clear list", "Call back",
"Messages (SMS)", "Open the Messages app", "Show the latest SMS here", "No SMS",
"This phone does not allow reading SMS. Use the Messages app.",
"If a second call arrives during a call, the caller gets a busy signal.",
"Also disable call waiting with the carrier (#43#)"]
T["it"] += ["Chiamate perse e bloccate", "Persa", "Buttata giù", "Bloccata", "Occupato (era già al telefono)",
"Nessuna chiamata persa", "Svuota elenco", "Richiama",
"Messaggi (SMS)", "Apri l'app Messaggi", "Mostra qui gli ultimi SMS", "Nessun SMS",
"Questo telefono non permette di leggere gli SMS. Usa l'app Messaggi.",
"Se arriva una seconda chiamata durante una telefonata, chi chiama trova occupato.",
"Disattiva anche l'avviso di chiamata dell'operatore (#43#)"]
T["es"] += ["Llamadas perdidas y bloqueadas", "Perdida", "Colgada", "Bloqueada", "Ocupado (ya estaba al teléfono)",
"No hay llamadas perdidas", "Vaciar lista", "Devolver llamada",
"Mensajes (SMS)", "Abrir la app de Mensajes", "Mostrar aquí los últimos SMS", "No hay SMS",
"Este teléfono no permite leer los SMS. Usa la app de Mensajes.",
"Si entra una segunda llamada durante una conversación, quien llama oirá ocupado.",
"Desactivar también la llamada en espera del operador (#43#)"]
T["fr"] += ["Appels manqués et bloqués", "Manqué", "Raccroché", "Bloqué", "Occupé (déjà au téléphone)",
"Aucun appel manqué", "Vider la liste", "Rappeler",
"Messages (SMS)", "Ouvrir l'application Messages", "Afficher ici les derniers SMS", "Aucun SMS",
"Ce téléphone ne permet pas de lire les SMS. Utilisez l'application Messages.",
"Si un deuxième appel arrive pendant une conversation, l'appelant entend occupé.",
"Désactiver aussi le double appel chez l'opérateur (#43#)"]
T["de"] += ["Verpasste und blockierte Anrufe", "Verpasst", "Aufgelegt", "Blockiert", "Besetzt (telefonierte bereits)",
"Keine verpassten Anrufe", "Liste leeren", "Zurückrufen",
"Nachrichten (SMS)", "Nachrichten-App öffnen", "Letzte SMS hier anzeigen", "Keine SMS",
"Dieses Telefon erlaubt das Lesen von SMS nicht. Bitte die Nachrichten-App verwenden.",
"Kommt während eines Gesprächs ein zweiter Anruf, hört der Anrufer besetzt.",
"Auch Anklopfen beim Netzbetreiber ausschalten (#43#)"]
T["pt"] += ["Chamadas perdidas e bloqueadas", "Perdida", "Desligada", "Bloqueada", "Ocupado (já estava ao telefone)",
"Nenhuma chamada perdida", "Limpar lista", "Ligar de volta",
"Mensagens (SMS)", "Abrir a app Mensagens", "Mostrar aqui as últimas SMS", "Nenhuma SMS",
"Este telefone não permite ler as SMS. Use a app Mensagens.",
"Se chegar uma segunda chamada durante uma conversa, quem liga ouve sinal de ocupado.",
"Desativar também a chamada em espera do operador (#43#)"]
T["ru"] += ["Пропущенные и заблокированные", "Пропущен", "Сброшен", "Заблокирован", "Занято (уже разговаривал)",
"Нет пропущенных звонков", "Очистить список", "Перезвонить",
"Сообщения (SMS)", "Открыть приложение «Сообщения»", "Показать здесь последние SMS", "Нет SMS",
"Этот телефон не разрешает читать SMS. Используйте приложение «Сообщения».",
"Если во время разговора поступит второй звонок, звонящий услышит «занято».",
"Также отключить ожидание вызова у оператора (#43#)"]
T["ar"] += ["المكالمات الفائتة والمحظورة", "فائتة", "مرفوضة", "محظورة", "مشغول (كان في مكالمة)",
"لا توجد مكالمات فائتة", "مسح القائمة", "معاودة الاتصال",
"الرسائل (SMS)", "فتح تطبيق الرسائل", "عرض آخر الرسائل هنا", "لا توجد رسائل",
"هذا الهاتف لا يسمح بقراءة الرسائل. استخدم تطبيق الرسائل.",
"إذا وصلت مكالمة ثانية أثناء مكالمة، سيسمع المتصل نغمة انشغال.",
"إيقاف خدمة انتظار المكالمات لدى المشغّل أيضًا (#43#)"]
T["hi"] += ["छूटी और ब्लॉक की गई कॉल", "छूटी", "काटी गई", "ब्लॉक", "व्यस्त (पहले से कॉल पर थे)",
"कोई छूटी कॉल नहीं", "सूची साफ़ करें", "वापस कॉल करें",
"संदेश (SMS)", "संदेश ऐप खोलें", "नवीनतम SMS यहाँ दिखाएँ", "कोई SMS नहीं",
"यह फ़ोन SMS पढ़ने की अनुमति नहीं देता। संदेश ऐप का उपयोग करें।",
"कॉल के दौरान दूसरी कॉल आने पर कॉल करने वाले को व्यस्त सुनाई देगा।",
"ऑपरेटर की कॉल वेटिंग भी बंद करें (#43#)"]
T["zh"] += ["未接和已拦截的来电", "未接", "已挂断", "已拦截", "占线（正在通话）",
"没有未接来电", "清空列表", "回拨",
"短信", "打开短信应用", "在此显示最新短信", "没有短信",
"此手机不允许读取短信，请使用短信应用。",
"通话中如有第二个来电，对方会听到占线。",
"同时关闭运营商的呼叫等待（#43#）"]

# --- v0.3: tema
KEYS += ["section_theme", "theme_light", "theme_dark"]
T[""] += ["Appearance", "Light", "Dark"]
T["it"] += ["Aspetto", "Chiaro", "Scuro"]
T["es"] += ["Apariencia", "Claro", "Oscuro"]
T["fr"] += ["Apparence", "Clair", "Sombre"]
T["de"] += ["Darstellung", "Hell", "Dunkel"]
T["pt"] += ["Aparência", "Claro", "Escuro"]
T["ru"] += ["Оформление", "Светлое", "Тёмное"]
T["ar"] += ["المظهر", "فاتح", "داكن"]
T["hi"] += ["रूप", "हल्का", "गहरा"]
T["zh"] += ["外观", "浅色", "深色"]

# --- v0.4: schermo, aggiunta dalla rubrica
KEYS += ["section_screen", "pref_keep_awake", "pref_keep_awake_hint", "brightness_title", "brightness_auto",
         "contact_add_from_book", "contact_add_manual"]
T[""] += ["Screen", "Never go to sleep", "The screen stays on all the time. Best to keep the phone on the charger.",
          "Brightness", "Automatic brightness (system)", "Add from address book", "Add manually"]
T["it"] += ["Schermo", "Non andare mai in standby", "Lo schermo resta sempre acceso. Meglio tenere il telefono in carica.",
            "Luminosità", "Luminosità automatica (di sistema)", "Aggiungi dalla rubrica", "Aggiungi a mano"]
T["es"] += ["Pantalla", "No suspender nunca", "La pantalla permanece siempre encendida. Mejor dejar el teléfono cargando.",
            "Brillo", "Brillo automático (del sistema)", "Añadir desde la agenda", "Añadir a mano"]
T["fr"] += ["Écran", "Ne jamais se mettre en veille", "L'écran reste toujours allumé. Mieux vaut laisser le téléphone en charge.",
            "Luminosité", "Luminosité automatique (système)", "Ajouter depuis le répertoire", "Ajouter manuellement"]
T["de"] += ["Bildschirm", "Nie in den Standby", "Der Bildschirm bleibt immer an. Das Telefon am besten am Ladegerät lassen.",
            "Helligkeit", "Automatische Helligkeit (System)", "Aus dem Adressbuch hinzufügen", "Manuell hinzufügen"]
T["pt"] += ["Ecrã", "Nunca entrar em espera", "O ecrã fica sempre ligado. É melhor manter o telefone a carregar.",
            "Brilho", "Brilho automático (do sistema)", "Adicionar da agenda", "Adicionar manualmente"]
T["ru"] += ["Экран", "Никогда не засыпать", "Экран всегда включён. Лучше держать телефон на зарядке.",
            "Яркость", "Автояркость (системная)", "Добавить из телефонной книги", "Добавить вручную"]
T["ar"] += ["الشاشة", "عدم الدخول في وضع السكون أبدًا", "تبقى الشاشة مضاءة دائمًا. يُفضّل إبقاء الهاتف على الشاحن.",
            "السطوع", "سطوع تلقائي (النظام)", "إضافة من دفتر العناوين", "إضافة يدويًا"]
T["hi"] += ["स्क्रीन", "कभी स्लीप मोड में न जाए", "स्क्रीन हमेशा चालू रहेगी। फ़ोन को चार्जर पर रखना बेहतर है।",
            "चमक", "स्वचालित चमक (सिस्टम)", "फ़ोन बुक से जोड़ें", "हाथ से जोड़ें"]
T["zh"] += ["屏幕", "永不休眠", "屏幕将始终亮着，建议让手机保持充电。",
            "亮度", "自动亮度（系统）", "从通讯录添加", "手动添加"]

# --- v0.4: tasto accensione
KEYS += ["pref_wake_on_power", "pref_wake_on_power_hint"]
T[""] += ["Ignore the power button", "If the power button is pressed, the screen turns back on by itself. Holding the button still turns the phone off."]
T["it"] += ["Ignora il tasto di accensione", "Se viene premuto il tasto di accensione, lo schermo si riaccende da solo. Tenendolo premuto si può comunque spegnere il telefono."]
T["es"] += ["Ignorar el botón de encendido", "Si se pulsa el botón de encendido, la pantalla vuelve a encenderse sola. Manteniéndolo pulsado se puede apagar el teléfono."]
T["fr"] += ["Ignorer le bouton marche/arrêt", "Si le bouton marche/arrêt est pressé, l'écran se rallume tout seul. Un appui long permet toujours d'éteindre le téléphone."]
T["de"] += ["Ein-/Aus-Taste ignorieren", "Wird die Ein-/Aus-Taste gedrückt, schaltet sich der Bildschirm von selbst wieder ein. Langes Drücken schaltet das Telefon weiterhin aus."]
T["pt"] += ["Ignorar o botão de ligar", "Se o botão de ligar for premido, o ecrã volta a acender-se sozinho. Mantendo-o premido continua a ser possível desligar o telefone."]
T["ru"] += ["Игнорировать кнопку питания", "Если нажать кнопку питания, экран сам включится снова. Долгим нажатием телефон по-прежнему можно выключить."]
T["ar"] += ["تجاهل زر التشغيل", "عند الضغط على زر التشغيل تعود الشاشة للعمل تلقائيًا. يمكن إيقاف الهاتف بالضغط المطوّل على الزر."]
T["hi"] += ["पावर बटन को अनदेखा करें", "पावर बटन दबाने पर स्क्रीन अपने-आप फिर चालू हो जाएगी। बटन देर तक दबाकर फ़ोन फिर भी बंद किया जा सकता है।"]
T["zh"] += ["忽略电源键", "按下电源键后屏幕会自动重新亮起。长按电源键仍可关机。"]

# --- v0.5: donazioni
KEYS += ["buy_coffee"]
T[""] += ["☕ Buy me a coffee"]
T["it"] += ["☕ Offrimi un caffè"]
T["es"] += ["☕ Invítame a un café"]
T["fr"] += ["☕ Offre-moi un café"]
T["de"] += ["☕ Spendier mir einen Kaffee"]
T["pt"] += ["☕ Paga-me um café"]
T["ru"] += ["☕ Угостить меня кофе"]
T["ar"] += ["☕ اشترِ لي قهوة"]
T["hi"] += ["☕ मुझे एक कॉफ़ी पिलाएँ"]
T["zh"] += ["☕ 请我喝杯咖啡"]

# --- v0.6: liste dalla rubrica, ritaglio foto
KEYS += ["number_add_from_book", "photo_adjust", "crop_hint", "crop_use"]
T[""] += ["From address book", "Adjust photo", "Drag to move the photo, pinch with two fingers to zoom in or out.", "Use this photo"]
T["it"] += ["Dalla rubrica", "Sistema la foto", "Trascina per spostare la foto, allarga o stringi con due dita per ingrandire o ridurre.", "Usa questa foto"]
T["es"] += ["De la agenda", "Ajustar la foto", "Arrastra para mover la foto y pellizca con dos dedos para ampliar o reducir.", "Usar esta foto"]
T["fr"] += ["Depuis le répertoire", "Ajuster la photo", "Faites glisser pour déplacer la photo, pincez à deux doigts pour zoomer ou dézoomer.", "Utiliser cette photo"]
T["de"] += ["Aus dem Adressbuch", "Foto anpassen", "Zum Verschieben ziehen, mit zwei Fingern vergrößern oder verkleinern.", "Dieses Foto verwenden"]
T["pt"] += ["Da agenda", "Ajustar a foto", "Arraste para mover a foto e use dois dedos para ampliar ou reduzir.", "Usar esta foto"]
T["ru"] += ["Из телефонной книги", "Настроить фото", "Перетащите, чтобы сдвинуть фото; сведите или разведите два пальца, чтобы изменить масштаб.", "Использовать это фото"]
T["ar"] += ["من دفتر العناوين", "ضبط الصورة", "اسحب لتحريك الصورة، وباعد أو قرّب إصبعين للتكبير أو التصغير.", "استخدام هذه الصورة"]
T["hi"] += ["फ़ोन बुक से", "फ़ोटो ठीक करें", "फ़ोटो खिसकाने के लिए खींचें, बड़ा या छोटा करने के लिए दो उँगलियों से पिंच करें।", "यह फ़ोटो लगाएँ"]
T["zh"] += ["从通讯录", "调整照片", "拖动以移动照片，用两根手指缩放。", "使用这张照片"]

def esc(s):
    s = escape(s).replace("'", "\\'").replace('"', '\\"').replace("\n", "\\n")
    if s.startswith("@") or s.startswith("?"): s = "\\" + s
    return s

base = os.path.join(os.path.dirname(os.path.abspath(__file__)), "app/src/main/res")
for lang, vals in T.items():
    assert len(vals) == len(KEYS), (lang, len(vals), len(KEYS))
    d = os.path.join(base, "values" + ("-" + lang if lang else ""))
    os.makedirs(d, exist_ok=True)
    with open(os.path.join(d, "strings.xml"), "w", encoding="utf-8") as f:
        f.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n')
        for k, v in zip(KEYS, vals):
            f.write(f'    <string name="{k}">{esc(v)}</string>\n')
        f.write("</resources>\n")
print("ok", len(T), "languages,", len(KEYS), "strings")
