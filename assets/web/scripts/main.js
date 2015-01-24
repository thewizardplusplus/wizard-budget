var GUI = {
	MINIMAL_CUSTOM_YEAR: -100,
	MAXIMAL_CUSTOM_YEAR: 100,
	DAYS_IN_CUSTOM_YEAR: 300,

	hideMainMenu: function() {
		var event = new CustomEvent('touchend');
		$('.backdrop').get(0).dispatchEvent(event);
	},
	refresh: function() {
		activity.updateWidget();
		PUSH({url: 'history.html'});
	},
	back: function() {
		if (!/\bhistory\b/.test(window.location)) {
			PUSH({url: 'history.html'});
		} else {
			var remove_dialog = $('#remove-dialog');
			if (remove_dialog.hasClass('active')) {
				remove_dialog.removeClass('active');
			} else if ($('.popover').hasClass('visible')) {
				this.hideMainMenu();
			} else {
				activity.quit();
			}
		}
	}
};

$(document).ready(
	function() {
		function LoadActiveSpending() {
			var json = activity.getSetting('active_spending');
			return JSON.parse(json);
		}
		function SaveActiveSpending(active_spending) {
			var json = JSON.stringify(active_spending);
			activity.setSetting('active_spending', json);
		}
		function UpdateSpendingList() {
			var spendings_sum_view = $('.spendings-sum-view');
			var spendings_sum = spending_manager.getSpendingsSum();
			spendings_sum_view.text(spendings_sum);
			if (spendings_sum <= 0) {
				spendings_sum_view.addClass('excess').removeClass('lack');
			} else {
				spendings_sum_view.addClass('lack').removeClass('excess');
			}

			var spending_list = $('.spending-list');
			spending_list.empty();

			var raw_spendings = spending_manager.getAllSpendings();
			var spendings = JSON.parse(raw_spendings);
			spendings.map(
				function(spending) {
					spending_list.append(
						'<li class = "table-view-cell media">'
							+ '<button '
								+ 'class = "btn second-list-button '
									+ 'edit-spending-button"'
								+ 'data-spending-id = "' + spending.id + '"'
								+ 'data-income = "'
									+ (spending.amount < 0
										? 'true'
										: 'false') + '"'
								+ 'data-timestamp = "'
									+ spending.timestamp + '">'
								+ '<i class = "fa fa-pencil"></i>'
							+ '</button>'
							+ '<button '
								+ 'class = "btn remove-spending-button"'
								+ 'data-spending-id = "' + spending.id + '">'
								+ '<i class = "fa fa-trash"></i>'
							+ '</button>'
							+ '<span class = "media-object pull-left">'
								+ '<i class = "fa fa-'
									+ (spending.amount > 0
										? 'shopping-cart'
										: 'money')
									+ ' fa-2x"></i>'
								+ '</span>'
							+ '<div class = "media-body">'
								+ '<p>'
									+ '<span class = "underline">'
										+ '<strong>'
											+ '<span class = "date-view">'
												+ spending.date
											+ '</span>'
										+ '</strong> '
										+ '<span class = "time-view">'
											+ spending.time
										+ '</span>:'
									+ '</span>'
								+ '</p>'
								+ '<p>'
									+ '<span class = "amount-view">'
										+ Math.abs(spending.amount)
									+ '</span> '
									+ '<i class = "fa fa-ruble"></i>'
									+ (spending.comment.length
										? ' &mdash; '
											+ '<em>'
												+ '<span '
													+ 'class = "comment-view">'
													+ spending.comment
												+ '</span>'
											+ '</em>'
										: '')
								+ '.</p>'
							+ '</div>'
						+ '</li>'
					);
				}
			);

			var remove_dialog = $('#remove-dialog');
			$('.remove-spending-button', remove_dialog).click(
				function() {
					var active_spending = LoadActiveSpending();
					if ($.type(active_spending) !== "null") {
						spending_manager.deleteSpending(active_spending.id);
						SaveActiveSpending(null);
						activity.updateWidget();

						PUSH({url: 'history.html'});
					}
				}
			);
			var remove_dialog_date_view = $('.date-view', remove_dialog);
			var remove_dialog_time_view = $('.time-view', remove_dialog);
			var remove_dialog_amount_view = $('.amount-view', remove_dialog);
			var remove_dialog_comment_view = $('.comment-view', remove_dialog);

			$('.edit-spending-button', spending_list).click(
				function() {
					var button = $(this);

					active_spending = {};
					active_spending.id = parseInt(button.data('spending-id'));
					active_spending.income_flag =
						button.data('income')
							? true
							: null;

					var timestamp = moment(
						parseInt(button.data('timestamp')) * 1000
					);
					var list_item = button.parent();
					if (activity.getSetting('use_custom_date') == 'true') {
						var custom_date_parts =
							$('.date-view', list_item)
							.text()
							.split('.');
						active_spending.custom_day = custom_date_parts[0];
						active_spending.custom_year = custom_date_parts[1];
					} else {
						active_spending.date = timestamp.format('YYYY-MM-DD');
					}
					active_spending.time = timestamp.format('HH:mm');

					active_spending.amount =
						$('.amount-view', list_item)
						.text();
					active_spending.comment =
						$('.comment-view', list_item)
						.text();

					SaveActiveSpending(active_spending);
					PUSH({url: 'editor.html'});
				}
			);
			$('.remove-spending-button', spending_list).click(
				function() {
					var button = $(this);

					active_spending = {};
					active_spending.id = parseInt(button.data('spending-id'));
					SaveActiveSpending(active_spending);

					var list_item = button.parent();
					var date = $('.date-view', list_item).text();
					var time = $('.time-view', list_item).text();
					var amount = $('.amount-view', list_item).text();
					var comment = $('.comment-view', list_item).text();

					remove_dialog_date_view.text(date);
					remove_dialog_time_view.text(time);
					remove_dialog_amount_view.text(amount);
					if (comment.length) {
						remove_dialog_comment_view.html(' &mdash; ' + comment);
					}

					remove_dialog.addClass('active');
				}
			);
		}
		function UpdateControlButtons() {
			$('.backup-button').click(
				function() {
					spending_manager.backup();
					GUI.hideMainMenu();
				}
			);
			$('.restore-button').click(
				function() {
					activity.selectBackupForRestore();
					GUI.hideMainMenu();
				}
			);
			$('.settings-button').click(
				function() {
					activity.openSettings();
					GUI.hideMainMenu();
				}
			);
		}
		function UpdateIndexPage() {
			UpdateControlButtons();
			UpdateSpendingList();
		}
		function UpdateEditorPage() {
			var active_spending = LoadActiveSpending();

			var edit_spending_button = $('form .edit-spending-button');
			if ($.type(active_spending) === "null") {
				$('.title').text('Add');
				$('.button-icon', edit_spending_button)
					.removeClass('fa-save')
					.addClass('fa-plus');
				$('.button-text', edit_spending_button).text('Add');
			} else {
				$('.title').text('Edit');
				$('.button-icon', edit_spending_button)
					.removeClass('fa-plus')
					.addClass('fa-save');
				$('.button-text', edit_spending_button).text('Save');
			}

			var custom_day_editor = $('.custom-day-editor');
			var custom_year_editor = $('.custom-year-editor');
			custom_day_editor.change(
				function() {
					var year = Math.abs(parseInt(custom_year_editor.val()));
					if (parseInt($(this).val()) >= 0) {
						custom_year_editor.val(year);
					} else {
						custom_year_editor.val(-year);
					}
				}
			);
			custom_year_editor.change(
				function() {
					var day = Math.abs(parseInt(custom_day_editor.val()));
					if (parseInt($(this).val()) >= 0) {
						custom_day_editor.val(day);
					} else {
						custom_day_editor.val(-day);
					}
				}
			);

			var current_timestamp = moment();
			var date_editor = $('.date-editor');
			if ($.type(active_spending) !== "null") {
				if (activity.getSetting('use_custom_date') == 'true') {
					for (
						var day = -GUI.DAYS_IN_CUSTOM_YEAR;
						day <= GUI.DAYS_IN_CUSTOM_YEAR;
						day++
					) {
						if (day == 0) {
							continue;
						}

						var formatted_day = Math.abs(day).toString();
						formatted_day =
							(day < 0 ? '-' : '')
							+ (formatted_day.length == 1 ? '0' : '')
							+ formatted_day;

						custom_day_editor.append(
							'<option '
								+ 'value = "' + day + '"'
								+ (active_spending.custom_day == formatted_day
									? ' selected = "selected"'
									: '') + '>'
								+ formatted_day
							+ '</option>'
						);
					}
					custom_day_editor.show();

					for (
						var year = GUI.MINIMAL_CUSTOM_YEAR;
						year <= GUI.MAXIMAL_CUSTOM_YEAR;
						year++
					) {
						if (year == 0) {
							continue;
						}

						var formatted_year = Math.abs(year).toString();
						formatted_year =
							(year < 0 ? '-' : '')
							+ (formatted_year.length == 1 ? '0' : '')
							+ formatted_year;

						custom_year_editor.append(
							'<option '
								+ 'value = "' + year + '"'
								+ (active_spending.custom_year == formatted_year
									? ' selected = "selected"'
									: '') + '>'
								+ formatted_year
							+ '</option>'
						);
					}
					custom_year_editor.show();
				} else {
					date_editor.show();
					date_editor.val(active_spending.date);
				}
			}

			var time_editor = $('.time-editor');
			if ($.type(active_spending) !== "null") {
				time_editor.val(active_spending.time);
				time_editor.show();
			}

			if ($.type(active_spending) !== "null") {
				$('hr').show();
			}

			var amount_editor = $('.amount-editor');
			if ($.type(active_spending) !== "null") {
				amount_editor.val(active_spending.amount);
			}
			amount_editor.focus();

			var comment_editor = $('.comment-editor');
			if ($.type(active_spending) !== "null") {
				comment_editor.val(active_spending.comment);
			}

			var income_flag = $('.income-flag');
			if ($.type(active_spending) !== "null") {
				if (active_spending.income_flag) {
					income_flag.addClass('active');
				}
			}

			edit_spending_button.click(
				function() {
					var amount = Math.abs(parseFloat(amount_editor.val()));
					var comment = comment_editor.val();
					if (income_flag.hasClass('active')) {
						amount *= -1;
					}

					if ($.type(active_spending) === "null") {
						spending_manager.createSpending(amount, comment);
					} else {
						var date = '';
						if (activity.getSetting('use_custom_date') == 'true') {
							var custom_day = custom_day_editor.val();
							var custom_year = custom_year_editor.val();
							date = custom_day + '.' + custom_year;
						} else {
							date = date_editor.val();
						}
						var time = time_editor.val();
						spending_manager.updateSpending(
							active_spending.id,
							date,
							time,
							amount,
							comment
						);
					}

					SaveActiveSpending(null);
					activity.updateWidget();

					PUSH({url: 'history.html'});
				}
			);
		}

		window.addEventListener(
			'push',
			function(event) {
				if (/\bhistory\b/.test(event.detail.state.url)) {
					UpdateIndexPage();
					activity.setSetting('current_page', 'history');
				} else if (/\beditor\b/.test(event.detail.state.url)) {
					UpdateEditorPage();
					activity.setSetting('current_page', 'editor');
				} else if (/\bauthors\b/.test(event.detail.state.url)) {
					activity.setSetting('current_page', 'authors');
				}
			}
		);
		PUSH({url: activity.getSetting('current_page') + '.html'});
	}
);
