package com.sapuseven.untis.ui.pages.main

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope

@Deprecated(
	message = " There is no \"Main\" anymore.",
	replaceWith = ReplaceWith("Timetable()", "com.sapuseven.untis.ui.activities.timetable"),
	level = DeprecationLevel.ERROR
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(
) {
	val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
	val scope = rememberCoroutineScope()

	/*Drawer(
		drawerState = drawerState
		/*onShowTimetable = {
			it.let { element ->
				state.setDisplayedElement(element?.first, element?.second)
			}
		}*/
	) {
		val activeUser = viewModel.activeUser.collectAsState()

		AppScaffold(
			topBar = {
				CenterAlignedTopAppBar(
					title = { Text(viewModel.displayedName) },
					navigationIcon = {
						IconButton(onClick = { scope.launch {
							drawerState.open()
						}}) {
							Icon(
								imageVector = Icons.Outlined.Menu,
								contentDescription = stringResource(id = R.string.main_drawer_open)
							)
						}
					},
					actions = {
						if (BuildConfig.DEBUG)
							DebugDesclaimerAction()

						ProfileSelectorAction(
							users = viewModel.userList,
							currentSelection = activeUser.value,
							showProfileActions = true,
							onSelectionChange = {
								viewModel.switchUser(it)
							},
							onActionEdit = {
								viewModel.editUsers()
							}
						)
					}
				)
			}
		) { innerPadding ->
			Box(
				modifier = Modifier
					.padding(innerPadding)
					.fillMaxSize()
			) {
				val density = LocalDensity.current
				val insets = insetsPaddingValues()
				val navBarHeight = remember {
					with(density) {
						(insets.calculateBottomPadding() + 48.dp).toPx()
					}
				}

				Column {
					Text("Current user: ${activeUser.value?.id}")
					Button(onClick = { viewModel.toggleTheme() }) {
						Text(text = "Toggle theme")
					}
				}

				/*WeekViewCompose(
					events = state.weekViewEvents,
					onPageChange = { pageOffset ->
						state.onPageChange(pageOffset)
					},
					onReload = { pageOffset ->
						state.loadEvents(startDateForPageIndex(pageOffset))
					},
					onItemClick = { state.timetableItemDetailsDialog = it },
					startTime = state.weekViewPreferences.hourList.value.firstOrNull()?.startTime
						?: LocalTime.MIDNIGHT,
					endTime = state.weekViewPreferences.hourList.value.lastOrNull()?.endTime
						?: LocalTime.MIDNIGHT,
					endTimeOffset = navBarHeight,
					hourHeight = /*state.weekViewPreferences.hourHeight ?:*/ 72.dp,
					hourList = state.weekViewPreferences.hourList.value,
					dividerWidth = state.weekViewPreferences.dividerWidth,
					colorScheme = state.weekViewPreferences.colorScheme,
				)

				val timeColumnWidth = with(LocalDensity.current) {
					/*state.weekView.value?.config?.timeColumnWidth?.toDp()
						?: */48.dp
				}

				Text(
					text = state.lastRefreshText(),
					modifier = Modifier
						.align(Alignment.BottomStart)
						.padding(start = timeColumnWidth + 8.dp, bottom = 8.dp)
						.bottomInsets()
						.disabled(state.isAnonymous)
				)

				IconButton(
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.padding(end = 8.dp)
						.bottomInsets(),
					onClick = {
						state.showFeedback()
					}
				) {
					Icon(painter = painterResource(R.drawable.all_feedback), contentDescription = "Give feedback")
				}

				if (state.feedbackDialog)
					FeedbackDialog(
						onDismiss = { state.feedbackDialog = false }
					)

				if (state.isAnonymous) {
					Column(
						verticalArrangement = Arrangement.Center,
						horizontalAlignment = Alignment.CenterHorizontally,
						modifier = Modifier
							.fillMaxSize()
							.absolutePadding(left = 16.dp)
					) {
						Text(
							text = stringResource(id = R.string.main_anonymous_login_info_text),
							textAlign = TextAlign.Center,
							modifier = Modifier
								.padding(horizontal = 32.dp)
						)

						Button(
							onClick = state.onAnonymousSettingsClick,
							modifier = Modifier
								.padding(top = 16.dp)
						) {
							Text(text = stringResource(id = R.string.main_go_to_settings))
						}
					}
				}

				if (state.isLoading)
					CircularProgressIndicator(
						modifier = Modifier
							.align(Alignment.BottomEnd)
							.padding(8.dp)
					)*/
			}

			ReportsInfoBottomSheet()
		}
	}

	AnimatedVisibility(
		visible = viewModel.profileManagementDialog,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		/*ProfileManagementDialog(
			viewModel = viewModel,
			onDismiss = {
				viewModel.profileManagementDialog = false
			}
		)*/
	}*/
}

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Drawer(
	viewModel: MainDrawerViewModel = viewModel(),
	drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
//onShowTimetable: (Pair<PeriodElement?, String?>?) -> Unit,
	content: @Composable () -> Unit
) {
	val user by viewModel.user.collectAsState()
	val scope = rememberCoroutineScope()
	val drawerScrollState = rememberScrollState()

	var showElementPicker by remember {
		mutableStateOf<TimetableDatabaseInterface.Type?>(
			null
		)
	}

	var bookmarksElementPicker by remember {
		mutableStateOf<TimetableDatabaseInterface.Type?>(
			null
		)
	}

	val shortcutLauncher =
		rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
			val periodElement: PeriodElement? = activityResult.data?.let { intent ->
				Json.decodeFromString(
					PeriodElement.serializer(),
					intent.getStringExtra(MainActivity.EXTRA_STRING_PERIOD_ELEMENT) ?: ""
				)
			}

			periodElement?.let {
				//onShowTimetable(it to state.timetableDatabaseInterface.getLongName(it))
			}
		}

	/*LaunchedEffect(state.drawerState) {
		snapshotFlow { state.drawerState.isOpen }
			.distinctUntilChanged()
			.drop(1)
			.collect {
				Log.i("Sentry", "Drawer isOpen: ${state.drawerState.isOpen}")
				Breadcrumb().apply {
					category = "ui.drawer"
					level = SentryLevel.INFO
					setData("isOpen", state.drawerState.isOpen)
					Sentry.addBreadcrumb(this)
				}
			}
	}*/

	BackHandler(enabled = drawerState.isOpen) {
		scope.launch {
			drawerState.close()
		}
	}

	ModalNavigationDrawer(
		gesturesEnabled = viewModel.enableDrawerGestures,
		drawerState = drawerState,
		drawerContent = {
			ModalDrawerSheet(
				modifier = Modifier
					.width(320.dp) // default: 360.dp
					.fillMaxHeight()
					.verticalScroll(drawerScrollState)
			) {
				Spacer(modifier = Modifier.height(24.dp))

				DrawerText(stringResource(id = R.string.all_favourites))

				NavigationDrawerItem(
					icon = {
						Icon(
							painterResource(id = R.drawable.all_prefs_personal),
							contentDescription = null
						)
					},
					label = { Text(stringResource(id = R.string.all_personal_timetable)) },
					selected = viewModel.isPersonalTimetableDisplayed(),
					onClick = { scope.launch {
						drawerState.close()
						//todo onShowTimetable(state.personalTimetable)
					}},
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
				)

				var isBookmarkSelected = false
				user?.bookmarks?.forEach { bookmark ->
					val isDisplayed = false/*state.displayedElement.value?.let {
						it.id == bookmark.elementId && it.type == bookmark.elementType
					} == true*/
					isBookmarkSelected = isBookmarkSelected || isDisplayed

					NavigationDrawerItem(
						icon = {
							Icon(
								painter = painterResource(
									id = when (TimetableDatabaseInterface.Type.valueOf(
										bookmark.elementType
									)) {
										TimetableDatabaseInterface.Type.CLASS -> R.drawable.all_classes
										TimetableDatabaseInterface.Type.TEACHER -> R.drawable.all_teachers
										TimetableDatabaseInterface.Type.SUBJECT -> R.drawable.all_subject
										TimetableDatabaseInterface.Type.ROOM -> R.drawable.all_rooms
										else -> R.drawable.all_prefs_personal
									}
								),
								contentDescription = null
							)
						},
						badge = {
							IconButton(
								onClick = { viewModel.showBookmarkDeleteDialog(bookmark) }
							) {
								Icon(
									painter = painterResource(id = R.drawable.all_bookmark_remove),
									contentDescription = "Remove Bookmark"
								) //TODO: Extract String resource
							}
						},
						label = { Text(text = bookmark.displayName) },
						selected = isDisplayed,
						onClick = { scope.launch {
							drawerState.close()
							/*todoval items = state.timetableDatabaseInterface.getElements(
								TimetableDatabaseInterface.Type.valueOf(bookmark.elementType)
							)
							val item = items.find {
								it.id == bookmark.elementId && it.type == bookmark.elementType
							}
							onShowTimetable(
								item to state.timetableDatabaseInterface.getLongName(item!!)
							)*/
						}},
						modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
					)
				}

				NavigationDrawerItem(
					icon = {
						Icon(
							painterResource(id = R.drawable.all_add),
							contentDescription = null
						)
					},
					label = { Text(stringResource(id = R.string.maindrawer_bookmarks_add)) },
					selected = false,
					onClick = { scope.launch {
						drawerState.close()
						bookmarksElementPicker = TimetableDatabaseInterface.Type.CLASS
					}},
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
				)

				DrawerText(stringResource(id = R.string.nav_all_timetables))

				DrawerItems(
					isMessengerAvailable = false,//todo state.isMessengerAvailable(),
					disableTypeSelection = false,//todo state.isPersonalTimetableDisplayed() || isBookmarkSelected,
					displayedElement = null,//todo state.displayedElement.value,
					onTimetableClick = { item -> scope.launch {
						drawerState.close()
						showElementPicker = item.elementType
					}},
					onShortcutClick = { item -> scope.launch {
						drawerState.close()
						viewModel.onShortcutItemClick(item, shortcutLauncher)
					}}
				)
			}
		},
		content = content
	)

	/*AnimatedVisibility(
		visible = showElementPicker != null,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		ElementPickerDialogFullscreen(
			title = { /*TODO*/ },
			timetableDatabaseInterface = state.timetableDatabaseInterface,
			onDismiss = { showElementPicker = null },
			onSelect = { item ->
				item?.let {
					onShowTimetable(
						item to state.timetableDatabaseInterface.getLongName(it)
					)
				} ?: run {
					onShowTimetable(state.personalTimetable)
				}
			},
			initialType = showElementPicker
		)
	}

	AnimatedVisibility(
		visible = bookmarksElementPicker != null,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		ElementPickerDialogFullscreen(
			title = { Text(stringResource(id = R.string.maindrawer_bookmarks_add)) },
			timetableDatabaseInterface = state.timetableDatabaseInterface,
			hideTypeSelectionPersonal = true,
			onDismiss = { bookmarksElementPicker = null },
			onSelect = { item ->
				item?.let {
					if (state.createBookmark(item))
						onShowTimetable(item to state.timetableDatabaseInterface.getLongName(it))
				}
			},
			initialType = bookmarksElementPicker
		)
	}*/

	viewModel.bookmarkDeleteDialog?.let { bookmark ->
		AlertDialog(
			text = { Text(stringResource(id = R.string.main_dialog_delete_bookmark)) },
			onDismissRequest = { viewModel.dismissBookmarkDeleteDialog() },
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.deleteBookmark(bookmark)
					}) {
					Text(stringResource(id = R.string.all_delete))
				}
			},
			dismissButton = {
				TextButton(
					onClick = { viewModel.dismissBookmarkDeleteDialog() }) {
					Text(stringResource(id = R.string.all_cancel))
				}
			}
		)
	}
}*/
