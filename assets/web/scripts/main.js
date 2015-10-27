var LOADING_LOG_CLEAN_DELAY = 2000;
var LOADING_LOG = {
	getTypeMark: function(type) {
		if (type == 'success') {
			return '<i class = "fa fa-check-circle"></i>';
		} else if (type == 'error') {
			return '<i class = "fa fa-times-circle"></i>';
		} else {
			return '<i class = "fa fa-info-circle"></i>';
		}
	},
	getTypeClass: function(type) {
		if (type == 'success' || type == 'error') {
			return type;
		} else {
			return '';
		}
	},
	addMessage: function(message, type) {
		type = type || 'info';

		var loading_log = $('.loading-log');
		loading_log.show();
		loading_log.prepend(
			'<p '
				+ 'class = "'
					+ 'content-padded '
					+ LOADING_LOG.getTypeClass(type)
				+ '">'
				+ LOADING_LOG.getTypeMark(type) + ' '
				+ message
			+ '</p>'
		);
	},
	finish: function(callback, message, type) {
		callback = callback || function() {};
		message = message || 'All done.'
		type = type || 'success';

		LOADING_LOG.addMessage(message, type);

		setTimeout(
			function() {
				LOADING_LOG.clean();
				callback();
			},
			LOADING_LOG_CLEAN_DELAY
		);
	},
	clean: function() {
		var loading_log = $('.loading-log');
		loading_log.empty();
		loading_log.hide();
	}
};

var HOURS_VIEW_PRECISION = 2;
function ProcessHours() {
	var work_calendar = JSON.parse(activity.getSetting('work_calendar'));
	var worked_hours = JSON.parse(activity.getSetting('worked_hours'));

	var hours_start_date = moment(activity.getSetting('hours_start_date'));
	var hours_end_date = moment(activity.getSetting('hours_end_date'));

	var start_day = hours_start_date.date();
	var end_day = hours_end_date.date();

	var month_data = [];
	if (work_calendar) {
		month_data = work_calendar[hours_start_date.month()];
	}

	var current_date = new Date();
	var is_current_month = current_date.getMonth() == hours_start_date.month();

	var expected_hours = 0;
	var month_worked_hours = 0;
	var month_rest_days = 0;
	for (var day = start_day; day <= end_day; day++) {
		var day_type = month_data[day - 1];
		if (typeof day_type !== 'undefined') {
			if (!is_current_month || day <= current_date.getDate()) {
				if (day_type === 'ordinary') {
					expected_hours += 8;
				} else if (day_type === 'short') {
					expected_hours += 7;
				}
			} else {
				if (day_type === 'ordinary' || day_type === 'short') {
					month_rest_days++;
				}
			}
		}

		if (worked_hours) {
			var day_worked_hours = worked_hours[day.toString()];
			if (typeof day_worked_hours !== 'undefined') {
				month_worked_hours += day_worked_hours;
			}
		}
	}

	var difference = expected_hours - month_worked_hours;
	var working_off = difference / month_rest_days;
	var hours_data = {
		start_day: start_day,
		expected_hours: expected_hours,
		month_worked_hours: month_worked_hours,
		difference: difference,
		working_off: working_off,
		working_off_mode:
			(isNaN(working_off) || working_off == Number.NEGATIVE_INFINITY
				? 'none'
				: (working_off == Infinity
					? 'infinity'
					: 'normal'))
	};
	activity.setSetting('hours_data', JSON.stringify(hours_data));

	ShowHours(hours_data);
	activity.updateWidget();
}
function ShowHours(hours_data) {
	var hours_range_start = moment().date(hours_data.start_day).format('ll');
	$('.hours-range-start').text(hours_range_start);

	var hours_view = $('#hours-segment .hours-view');
	$('.expected-hours-view', hours_view).text(
		hours_data.expected_hours
	);
	$('.hours-worked-view', hours_view).text(
		hours_data.month_worked_hours.toFixed(HOURS_VIEW_PRECISION)
	);

	var difference_view = $('.difference-view', hours_view);
	difference_view.text(
		hours_data.difference.toFixed(HOURS_VIEW_PRECISION)
	);
	if (hours_data.difference <= 0) {
		difference_view.removeClass('lack').addClass('excess');
	} else {
		difference_view.removeClass('excess').addClass('lack');
	}

	var working_off_view = $('.working-off-view', hours_view);
	if (
		!isNaN(hours_data.working_off)
		&& hours_data.working_off != Number.NEGATIVE_INFINITY
	) {
		if (hours_data.working_off != Infinity) {
			working_off_view.text(
				hours_data.working_off.toFixed(HOURS_VIEW_PRECISION)
			);

			var working_off_limit = parseFloat(
				activity.getSetting('working_off_limit')
			);
			if (hours_data.working_off <= working_off_limit) {
				working_off_view.removeClass('lack').addClass('excess');
			} else {
				working_off_view.removeClass('excess').addClass('lack');
			}
		} else {
			working_off_view.html('&infin;');
			working_off_view.removeClass('excess').addClass('lack');
		}
	} else {
		working_off_view.html('&mdash;');
		working_off_view.removeClass('lack').addClass('excess');
	}
}

function RequestToHarvest(request_name, path) {
	var harvest_subdomain = activity.getSetting('harvest_subdomain');
	var url = 'https://' + harvest_subdomain + '.harvestapp.com' + path;

	var harvest_username = activity.getSetting('harvest_username');
	var harvest_password = activity.getSetting('harvest_password');
	var headers = {
		'Content-Type': 'application/json',
		Accept: 'application/json',
		Authorization:
			'Basic '
			+ Base64.encode(harvest_username + ':' + harvest_password)
	};

	activity.httpRequest(request_name, url, JSON.stringify(headers));
}
var HTTP_HANDLERS = {
	harvest_user_id: function(data) {
		LOADING_LOG.addMessage('Start the Harvest user ID processing.');
		var parsed_data = JSON.parse(data);

		var user_id = parsed_data.user.id;
		var start = moment().startOf('month').format('YYYYMMDD');
		var end = moment().format('YYYYMMDD');
		var path =
			'/people/'
			+ user_id
			+ '/entries?from='
			+ start
			+ '&to='
			+ end;
		LOADING_LOG.addMessage(
			'The Harvest user ID processing has finished.',
			'success'
		);

		RequestToHarvest('harvest_time_entries', path);
	},
	harvest_time_entries: function(data) {
		LOADING_LOG.addMessage('Start the Harvest time entries processing.');
		var parsed_data = JSON.parse(data);

		var entries = Object.create(null);
		for (var i = 0; i < parsed_data.length; i++) {
			var entry = parsed_data[i].day_entry;
			var day = entry.spent_at.split('-')[2].replace(/^0/, '');
			if (!entries[day]) {
				entries[day] = 0;
			}

			entries[day] += entry.hours;
		}

		activity.setSetting('worked_hours', JSON.stringify(entries));
		LOADING_LOG.addMessage(
			'The Harvest time entries processing has finished.',
			'success'
		);

		activity.httpRequest(
			'work_calendar',
			'http://www.calend.ru/work/',
			JSON.stringify(null)
		);
	},
	work_calendar: function(data) {
		LOADING_LOG.addMessage('Start the work calendar processing.');
		var dom = $(data);

		var work_hours = [];
		$('.time_of_death > tbody > tr:not(:first-child) > td', dom).each(
			function() {
				var month = [];
				$('tr:nth-child(n+3)', $(this)).each(
					function() {
						$('td', $(this)).each(
							function(index) {
								var element = $(this);
								if (!element.hasClass('day')) {
									return;
								}

								var day_type = 'ordinary';
								if (element.hasClass('col5')) {
									if (index < 6) {
										day_type = 'holiday';
									} else {
										day_type = 'weekend';
									}
								} else if (element.hasClass('col6')) {
									day_type = 'short';
								}

								month.push(day_type);
							}
						);
					}
				);

				work_hours.push(month);
			}
		);

		activity.setSetting('work_calendar', JSON.stringify(work_hours));
		LOADING_LOG.addMessage(
			'The work calendar processing has finished.',
			'success'
		);

		ProcessHours();

		LOADING_LOG.finish(
			function() {
				$('.refresh-button').removeClass('disabled');
			}
		);
	}
};

var GUI = {
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
			} else {
				remove_dialog = $('#remove-buy-dialog');
				if (remove_dialog.hasClass('active')) {
					remove_dialog.removeClass('active');
				} else {
					if ($('.popover').hasClass('visible')) {
						this.hideMainMenu();
					} else {
						activity.quit();
					}
				}
			}
		}
	},
	addLoadingLogMessage: function(message) {
		LOADING_LOG.addMessage(message);
	},
	setHttpResult: function(request, data) {
		if (data.substr(0, 6) != 'error:') {
			LOADING_LOG.addMessage(
				'The "' + request + '" HTTP request has finished.',
				'success'
			);

			var handler = HTTP_HANDLERS[request];
			if (handler) {
				handler(data);
			}
		} else {
			LOADING_LOG.addMessage('Error: "' + data.substr(6) + '".', 'error');
		}
	},
	debug: function(message) {
		$('.debug').text(message);
	}
};

$(document).ready(
	function() {
		function LoadActiveSpending() {
			var json = activity.getSetting('active_spending');
			SaveActiveSpending(null);

			return JSON.parse(json);
		}
		function SaveActiveSpending(active_spending) {
			var json = JSON.stringify(active_spending);
			activity.setSetting('active_spending', json);
		}
		function LoadActiveBuy() {
			var json = activity.getSetting('active_buy');
			SaveActiveBuy(null);

			return JSON.parse(json);
		}
		function SaveActiveBuy(active_buy) {
			var json = JSON.stringify(active_buy);
			activity.setSetting('active_buy', json);
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
							+ '<span '
								+ 'class = "'
									+ 'media-object '
									+ 'pull-left '
									+ 'mark-container'
								+ '">'
								+ (spending.has_credit_card_tag
									? '<i class = "fa fa-credit-card mark"></i>'
									: '')
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
			$('.close-remove-dialog', remove_dialog).click(
				function() {
					remove_dialog.removeClass('active');
					SaveActiveSpending(null);

					return false;
				}
			);
			var remove_dialog_date_view = $('.date-view', remove_dialog);
			var remove_dialog_time_view = $('.time-view', remove_dialog);
			var remove_dialog_amount_view = $('.amount-view', remove_dialog);
			var remove_dialog_comment_view = $('.comment-view', remove_dialog);
			$('.remove-spending-button', remove_dialog).click(
				function() {
					var active_spending = LoadActiveSpending();
					if ($.type(active_spending) !== "null") {
						spending_manager.deleteSpending(active_spending.id);
						activity.updateWidget();

						PUSH({url: 'history.html'});
					}
				}
			);

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
					active_spending.date = timestamp.format('YYYY-MM-DD');
					active_spending.time = timestamp.format('HH:mm');

					var list_item = button.parent();
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
		function UpdateBuyList() {
			var costs_sum_view = $('.costs-sum-view');
			var costs_sum = buy_manager.getCostsSum();
			costs_sum_view.text(costs_sum);

			var buy_list = $('.buy-list');
			buy_list.empty();

			var raw_buys = buy_manager.getAllBuys();
			var buys = JSON.parse(raw_buys);
			buys.map(
				function(buy) {
					buy_list.append(
						'<li '
							+ 'class = "'
								+ 'table-view-cell '
								+ 'media '
								+ (buy.status
									? 'buyed'
									: 'not-buyed') + '"'
							+ 'data-id = "' + buy.id + '">'
							+ '<button '
								+ 'class = "btn second-list-button '
									+ 'edit-buy-button"'
								+ 'data-buy-id = "' + buy.id + '"'
								+ 'data-status = "' + buy.status + '">'
								+ '<i class = "fa fa-pencil"></i>'
							+ '</button>'
							+ '<button '
								+ 'class = "btn remove-buy-button"'
								+ 'data-buy-id = "' + buy.id + '">'
								+ '<i class = "fa fa-trash"></i>'
							+ '</button>'
							+ '<span '
								+ 'class = "'
									+ 'media-object '
									+ 'pull-left'
								+ '">'
								+ '<i '
									+ 'class = "'
										+ 'fa '
										+ 'fa-' + (buy.status
											? 'gift'
											: 'shopping-cart') + ' '
										+ 'fa-2x'
									+ '">'
								+ '</i>'
							+ '</span>'
							+ '<div class = "media-body">'
								+ '<p>'
									+ '<span class = "underline">'
										+ '<strong>'
											+ '<span class = "name-view">'
												+ buy.name
											+ '</span>:'
										+ '</strong>'
									+ '</span>'
								+ '</p>'
								+ '<p>'
									+ '<span class = "cost-view">'
										+ buy.cost
									+ '</span> '
									+ '<i class = "fa fa-ruble"></i>.'
								+ '</p>'
							+ '</div>'
						+ '</li>'
					);
				}
			);

			var remove_dialog = $('#remove-buy-dialog');
			$('.close-remove-dialog', remove_dialog).click(
				function() {
					remove_dialog.removeClass('active');
					SaveActiveBuy(null);

					return false;
				}
			);
			var remove_dialog_name_view = $('.name-view', remove_dialog);
			var remove_dialog_cost_view = $('.cost-view', remove_dialog);
			$('.remove-buy-button', remove_dialog).click(
				function() {
					var active_buy = LoadActiveBuy();
					if ($.type(active_buy) !== "null") {
						buy_manager.deleteBuy(active_buy.id);
						activity.updateWidget();

						PUSH({url: 'history.html'});
					}
				}
			);

			$('.edit-buy-button', buy_list).click(
				function() {
					var button = $(this);

					active_buy = {};
					active_buy.id = parseInt(button.data('buy-id'));
					active_buy.status =
						button.data('status')
							? true
							: null;

					var list_item = button.parent();
					active_buy.name = $('.name-view', list_item).text();
					active_buy.cost = $('.cost-view', list_item).text();

					SaveActiveBuy(active_buy);
					PUSH({url: 'buy_editor.html'});
				}
			);
			$('.remove-buy-button', buy_list).click(
				function() {
					var button = $(this);

					active_buy = {};
					active_buy.id = parseInt(button.data('buy-id'));
					SaveActiveBuy(active_buy);

					var list_item = button.parent();
					var name = $('.name-view', list_item).text();
					var cost = $('.cost-view', list_item).text();

					remove_dialog_name_view.text(name);
					remove_dialog_cost_view.text(cost);
					remove_dialog.addClass('active');
				}
			);

			buy_list.sortable(
				{
					draggable: '.not-buyed',
					handle: '.media-object',
					ghostClass: 'placeholder',
					onStart: function(event) {
						var item = $('li', buy_list)
							.filter(
								function() {
									return $(this).css('position') == 'fixed';
								}
							)
							.addClass('moving');

						setTimeout(
							function() {
								var position = item.offset();
								position.top -= 95;
								item.offset(position);
							},
							1
						);
					},
					onUpdate: function() {
						var order = $('li', buy_list).map(
							function() {
								return $(this).data('id');
							}
						);
						var serialized_order = JSON.stringify(order.get());
						buy_manager.updateBuyOrder(serialized_order);
					}
				}
			);
		}
		function UpdateControlButtons() {
			$('.backup-button').click(
				function() {
					GUI.hideMainMenu();

					var filename = backup_manager.backup();
					if (
						activity.getSetting('save_backup_to_dropbox') == "true"
						&& filename.length
					) {
						activity.saveToDropbox(filename);
					}
				}
			);
			$('.restore-button').click(
				function() {
					GUI.hideMainMenu();
					activity.selectBackupForRestore();
				}
			);
			$('.settings-button').click(
				function() {
					GUI.hideMainMenu();
					activity.openSettings();
				}
			);

			$('.add-button').click(
				function() {
					if ($('#buys-segment').hasClass('active')) {
						PUSH({url: 'buy_editor.html'});
					} else {
						PUSH({url: 'editor.html'});
					}
				}
			);
		}
		function SetCurrentSegment(current_segment) {
			$('.control-item, .control-content').removeClass('active');
			$('.' + current_segment + '-segment').addClass('active');
		}
		function UpdateHoursDataIfNeed() {
			if (
				activity.getSetting('current_segment') == 'hours'
				&& activity.getSetting('analysis_harvest') === 'true'
			) {
				if (activity.getSetting('need_update_hours') === 'true') {
					activity.setSetting('need_update_hours', 'false');
					$('.refresh-button').click();
				}
			}
		}
		function UpdateSegments() {
			var RESET_SEGMENT_TIMEOUT = 100;

			var current_segment = activity.getSetting('current_segment');
			if (
				(current_segment == 'stats'
				&& activity.getSetting('collect_stats') !== 'true')
				|| (current_segment == 'hours'
				&& activity.getSetting('analysis_harvest') !== 'true')
			) {
				current_segment = 'history';
				activity.setSetting('current_segment', 'history');
			}
			SetCurrentSegment(current_segment);

			if (activity.getSetting('collect_stats') !== 'true') {
				$('.stats-segment').hide();
			}
			if (activity.getSetting('analysis_harvest') !== 'true') {
				$('.hours-segment').hide();
			}

			var add_button = $('.add-button');
			if (current_segment == 'stats' || current_segment == 'hours') {
				add_button.hide();
			} else {
				add_button.show();
			}

			var refresh_button = $('.refresh-button');
			if (current_segment == 'hours') {
				refresh_button.show();
			} else {
				refresh_button.hide();
			}

			$('.control-item').on(
				'touchend',
				function() {
					var self = $(this);
					if (self.hasClass('buys-segment')) {
						activity.setSetting('current_segment', 'buys');
						add_button.show();
						refresh_button.hide();
					} else if (self.hasClass('stats-segment')) {
						if (
							activity.getSetting('collect_stats') === 'true'
						) {
							activity.setSetting('current_segment', 'stats');
							add_button.hide();
							refresh_button.hide();
						} else {
							activity.setSetting('current_segment', 'history');
							add_button.show();
							refresh_button.hide();

							setTimeout(
								function() {
									SetCurrentSegment('history');
								},
								RESET_SEGMENT_TIMEOUT
							);
						}
					} else if (self.hasClass('hours-segment')) {
						if (
							activity.getSetting('analysis_harvest') === 'true'
						) {
							activity.setSetting('current_segment', 'hours');
							add_button.hide();
							refresh_button.show();
						} else {
							activity.setSetting('current_segment', 'history');
							add_button.show();
							refresh_button.hide();

							setTimeout(
								function() {
									SetCurrentSegment('history');
								},
								RESET_SEGMENT_TIMEOUT
							);
						}
					} else {
						activity.setSetting('current_segment', 'history');
						add_button.show();
						refresh_button.hide();
					}
				}
			);
		}
		function DrawStatsView(number_of_last_days, comment_prefix) {
			var spendings_sum_view = $('.stats-sum-view');
			var spendings_sum = spending_manager.getStatsSum(
				number_of_last_days,
				comment_prefix
			);
			spendings_sum_view.text(spendings_sum);

			var selected_tag_list = $('.selected-tag-list');
			selected_tag_list.empty();

			var selected_tags =
				comment_prefix
				.split(',')
				.map(
					function(tag) {
						return tag.trim();
					}
				)
				.filter(
					function(tag) {
						return tag.length != 0;
					}
				);
			var selected_tags_copy = selected_tags.slice();
			selected_tags.unshift('root');
			selected_tags.map(
				function(tag, index) {
					var list_item = '';
					if (index != 0) {
						list_item += ' / ';
					}
					if (index < selected_tags.length - 1) {
						list_item +=
							'<button '
								+ 'class = "btn btn-info unselect-tag-button"'
								+ 'data-tag = "' + escape(tag) + '">'
								+ tag
							+ '</button>';
					} else {
						list_item += tag;
					}

					selected_tag_list.append(list_item);
				}
			);
			$('.unselect-tag-button', selected_tag_list).click(
				function() {
					var self = $(this);
					var tag = unescape(self.data('tag'));

					var new_comment_prefix = '';
					var index = selected_tags_copy.lastIndexOf(tag);
					if (index != -1) {
						new_comment_prefix =
							selected_tags_copy
							.slice(
								0,
								index + 1
							)
							.join(', ');
					}
					activity.setSetting('stats_tags', new_comment_prefix);

					DrawStatsView(number_of_last_days, new_comment_prefix);
				}
			);

			var stats_view = $('.stats-view tbody');
			stats_view.empty();

			var raw_stats = spending_manager.getStats(
				number_of_last_days,
				comment_prefix
			);
			var stats = JSON.parse(raw_stats);
			stats = stats.sort(
				function(first, second) {
					return second.sum - first.sum;
				}
			);

			var maximal_sum = 0;
			stats.forEach(
				function(row) {
					if (row.sum > maximal_sum) {
						maximal_sum = row.sum;
					}
				}
			);
			stats.forEach(
				function(row) {
					var percents = 100 * row.sum / maximal_sum;
					var percents_string =
						percents
						.toFixed(2)
						.replace(/(\.0)?0+$/g, '$1');
					stats_view.append(
						'<tr>'
							+ '<td class = "tag-column">'
								+ '<button '
									+ 'class = "'
										+ 'btn '
										+ 'btn-info '
										+ 'select-tag-button'
									+ '"'
									+ 'data-tag = "' + escape(row.tag) + '"'
									+ (row.is_rest
										? 'disabled = "disabled"'
										: '') + '>'
									+ (row.is_rest
										? '<em>rest</em>'
										: row.tag)
								+ '</button>'
							+ '</td>'
							+ '<td class = "sum-column">'
								+ row.sum + ' '
									+ '<i class = "fa fa-ruble"></i><br />'
								+ '<em>(' + percents_string + '%)</em>'
							+ '</td>'
							+ '<td class = "view-column">'
								+ '<progress '
									+ 'max = "' + maximal_sum + '"'
									+ 'value = "' + row.sum + '">'
								+ '</progress>'
							+ '</td>'
						+ '</tr>'
					);
				}
			);

			$('.select-tag-button', stats_view).click(
				function() {
					var self = $(this);
					var tag = unescape(self.data('tag'));

					var new_comment_prefix = comment_prefix;
					if (new_comment_prefix.length) {
						new_comment_prefix += ', ';
					}
					new_comment_prefix += tag;
					activity.setSetting('stats_tags', new_comment_prefix);

					DrawStatsView(number_of_last_days, new_comment_prefix);
				}
			);
		}
		function UpdateStats() {
			var STATS_RANGE_SAVING_TIMEOUT = 500;

			$('.stats-range-form').on(
				'submit',
				function(event) {
					event.preventDefault();
					return false;
				}
			);

			var number_of_last_days = activity.getSetting('stats_range');
			var range_editor = $('.stats-range-editor');
			range_editor.val(number_of_last_days);

			var integral_range = parseInt(number_of_last_days);
			if (integral_range == 0) {
				$('.stats-range-view').hide();
				$('.stats-range-dummy').show();
			} else {
				$('.stats-range-view').show();

				var stats_range_start =
					moment()
					.subtract(integral_range, 'd')
					.format('ll');
				$('.stats-range-start').text(stats_range_start);

				$('.stats-range-dummy').hide();
			}
			$('.stats-range-end').text(moment().format('ll'));

			var comment_prefix = activity.getSetting('stats_tags');

			var range_update_timer = null;
			range_editor.on(
				'keyup',
				function() {
					clearTimeout(range_update_timer);
					range_update_timer = setTimeout(
						function() {
							var number_of_last_days = range_editor.val();
							activity.setSetting(
								'stats_range',
								number_of_last_days
							);

							var integral_range = parseInt(number_of_last_days);
							if (integral_range == 0) {
								$('.stats-range-view').hide();
								$('.stats-range-dummy').show();
							} else {
								$('.stats-range-view').show();

								var stats_range_start =
									moment()
									.subtract(integral_range, 'd')
									.format('ll');
								$('.stats-range-start').text(stats_range_start);

								$('.stats-range-dummy').hide();
							}

							DrawStatsView(integral_range, comment_prefix);
						},
						STATS_RANGE_SAVING_TIMEOUT
					);
				}
			);

			DrawStatsView(integral_range, comment_prefix);
		}
		function UpdateHours() {
			$('.hours-range-form').on(
				'submit',
				function(event) {
					event.preventDefault();
					return false;
				}
			);

			var hours_start_date = activity.getSetting('hours_start_date');
			var hours_start_date_editor = $('.hours-range-start-editor');
			hours_start_date_editor.val(hours_start_date);

			var hours_end_date = activity.getSetting('hours_end_date');
			var hours_end_date_editor = $('.hours-range-end-editor');
			hours_end_date_editor.val(hours_end_date);

			hours_start_date_editor.change(
				function() {
					var self = $(this);
					var new_date = self.val();
					var wrapped_new_date = moment(new_date);
					var current_year = moment().year();
					if (wrapped_new_date.year() != current_year) {
						wrapped_new_date = wrapped_new_date.year(current_year);
						new_date = wrapped_new_date.format('YYYY-MM-DD');
						self.val(new_date);
					}

					activity.setSetting('hours_start_date', new_date);

					var hours_end_date = moment(hours_end_date_editor.val());
					var new_month = wrapped_new_date.month();
					if (hours_end_date.month() != new_month) {
						var new_hours_end_date =
							hours_end_date
							.month(new_month)
							.endOf('month');
						var formatted_new_hours_end_date =
							new_hours_end_date
							.format('YYYY-MM-DD');
						hours_end_date_editor.val(formatted_new_hours_end_date);
						activity.setSetting(
							'hours_end_date',
							formatted_new_hours_end_date
						);
					}

					ProcessHours();
				}
			);
			hours_end_date_editor.change(
				function() {
					var self = $(this);
					var new_date = self.val();
					var wrapped_new_date = moment(new_date);
					var current_year = moment().year();
					if (wrapped_new_date.year() != current_year) {
						new_date =
							wrapped_new_date
							.year(current_year)
							.format('YYYY-MM-DD');
						self.val(new_date);
					}

					activity.setSetting('hours_end_date', new_date);

					var hours_start_date = moment(
						hours_start_date_editor.val()
					);
					var new_month = wrapped_new_date.month();
					if (hours_start_date.month() != new_month) {
						var new_hours_start_date =
							hours_start_date
							.month(new_month)
							.startOf('month');
						var formatted_new_hours_start_date =
							new_hours_start_date
							.format('YYYY-MM-DD');
						hours_start_date_editor.val(
							formatted_new_hours_start_date
						);
						activity.setSetting(
							'hours_start_date',
							formatted_new_hours_start_date
						);
					}

					ProcessHours();
				}
			);

			$('.refresh-button').click(
				function() {
					var self = $(this);
					if (!self.hasClass('disabled')) {
						self.addClass('disabled');
					} else {
						return;
					}

					RequestToHarvest('harvest_user_id', '/account/who_am_i');
				}
			);

			ProcessHours();
		}
		function UpdateIndexPage() {
			UpdateControlButtons();
			UpdateSegments();
			UpdateSpendingList();
			UpdateBuyList();
			UpdateStats();
			UpdateHours();
			UpdateHoursDataIfNeed();
		}
		function UpdateEditorPage() {
			var CORRECT_SPENDING_PRECISION = 6;

			var active_spending = LoadActiveSpending();

			var edit_spending_button = $('form .edit-spending-button');
			var spending_type = $('form .spending-type');
			if ($.type(active_spending) === "null") {
				$('.title').text('Add spending');
				$('.button-icon', edit_spending_button)
					.removeClass('fa-save')
					.addClass('fa-plus');
				$('.button-text', edit_spending_button).text('Add');
			} else {
				$('.title').text('Edit spending');
				$('.button-icon', edit_spending_button)
					.removeClass('fa-plus')
					.addClass('fa-save');
				$('.button-text', edit_spending_button).text('Save');
				$('option[value=sum]', spending_type).hide();
			}

			var date_editor = $('.date-editor');
			if ($.type(active_spending) !== "null") {
				date_editor.val(active_spending.date);
				date_editor.show();
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

			var raw_tags = spending_manager.getSpendingTags();
			var tags = JSON.parse(raw_tags);

			var raw_buy_names = buy_manager.getBuyNames();
			var buy_names = JSON.parse(raw_buy_names);
			tags = tags.concat(buy_names);

			var raw_priorities_tags = spending_manager.getPrioritiesTags();
			var priorities_tags = JSON.parse(raw_priorities_tags);

			var default_tags =
				$.type(active_spending) !== "null"
					? active_spending.comment.split(',')
					: [];
			var tags_editor = new WizardTags(
				'.tags-editor',
				{
					tags: tags,
					search_mode: 'words',
					sort: 'priorities-desc',
					priorities_tags: priorities_tags,
					default_tags: default_tags,
					separators: ',',
					only_unique: true
				}
			);

			if (
				$.type(active_spending) !== "null"
				&& active_spending.income_flag
			) {
				spending_type.val('income');
			} else {
				spending_type.val('spending');
			}

			edit_spending_button.click(
				function() {
					var signed_amount = parseFloat(amount_editor.val());
					var amount = Math.abs(signed_amount);

					tags_editor.addCurrentText();
					var tags = tags_editor.getTags();
					var comment = tags.join(', ');

					if (spending_type.val() == 'income') {
						amount *= -1;
					} else if (spending_type.val() == 'sum') {
						var sum = parseFloat(
							spending_manager.getSpendingsSum()
						);
						var difference = signed_amount - sum;
						amount = parseFloat(
							difference.toFixed(CORRECT_SPENDING_PRECISION)
						);
					}

					if ($.type(active_spending) === "null") {
						spending_manager.createSpending(amount, comment);
					} else {
						var date = date_editor.val();
						var time = time_editor.val();
						spending_manager.updateSpending(
							active_spending.id,
							date,
							time,
							amount,
							comment
						);
					}

					var serialized_tags = JSON.stringify(tags);
					buy_manager.mayBeBuy(serialized_tags);

					activity.updateWidget();
					PUSH({url: 'history.html'});

					return false;
				}
			);
		}
		function UpdateBuyEditorPage() {
			var active_buy = LoadActiveBuy();

			var edit_buy_button = $('form .edit-buy-button');
			if ($.type(active_buy) === "null") {
				$('.title').text('Add buy');
				$('.button-icon', edit_buy_button)
					.removeClass('fa-save')
					.addClass('fa-plus');
				$('.button-text', edit_buy_button).text('Add');
			} else {
				$('.title').text('Edit buy');
				$('.button-icon', edit_buy_button)
					.removeClass('fa-plus')
					.addClass('fa-save');
				$('.button-text', edit_buy_button).text('Save');
			}

			var name_editor = $('.name-editor');
			if ($.type(active_buy) !== "null") {
				name_editor.val(active_buy.name);
			}
			name_editor.focus();

			var cost_editor = $('.cost-editor');
			if ($.type(active_buy) !== "null") {
				cost_editor.val(active_buy.cost);
			}

			var status_flag = $('.status');
			if ($.type(active_buy) !== "null") {
				if (active_buy.status) {
					status_flag.addClass('active');
				}
			} else {
				status_flag.parent().hide();
			}

			edit_buy_button.click(
				function() {
					var name = name_editor.val();
					var cost = parseFloat(cost_editor.val());
					var status = status_flag.hasClass('active') ? 1 : 0;

					if ($.type(active_buy) === "null") {
						buy_manager.createBuy(name, cost);
					} else {
						buy_manager.updateBuy(
							active_buy.id,
							name,
							cost,
							status
						);
					}

					activity.updateWidget();
					PUSH({url: 'history.html'});

					return false;
				}
			);
		}
		function UpdateSmsPage() {
			var UPDATE_INPUT_BUTTON_TIMEOUT = 100;

			var sms_list = $('.sms-list');
			sms_list.empty();

			var raw_spendings = spending_manager.getSpendingsFromSms();
			var spendings = JSON.parse(raw_spendings);
			spendings.map(
				function(spending) {
					sms_list.append(
						'<li '
							+ 'class = "table-view-cell media" '
							+ 'data-timestamp = "'
								+ spending.timestamp
							+ '" '
							+ 'data-amount = "' + spending.amount + '" '
							+ 'data-residue = "' + spending.residue + '">'
							+ '<div class = "toggle import-flag">'
								+ '<div class = "toggle-handle"></div>'
							+ '</div>'
							+ '<span class = "'
								+ 'media-object '
								+ 'pull-left '
								+ 'mark-container'
							+ '">'
								+ '<i class = "fa fa-credit-card mark"></i>'
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
											+ spending.date
										+ '</strong>'
										+ ' ' + spending.time + ':'
									+ '</span>'
								+ '</p>'
								+ '<p>'
									+ Math.abs(spending.amount)
									+ '<i class = "fa fa-ruble"></i>.'
								+ '</p>'
							+ '</div>'
						+ '</li>'
					);
				}
			);

			var import_button = $('.import-sms-button');
			import_button.click(
				function() {
					var sms_data = [];
					$('li', sms_list).each(
						function() {
							var list_item = $(this);
							if ($('.import-flag.active', list_item).length) {
								sms_data.push(
									{
										timestamp: list_item.data('timestamp'),
										amount: list_item.data('amount'),
										residue: list_item.data('residue'),
									}
								);
							}
						}
					);

					var sms_data_in_string = JSON.stringify(sms_data);
					spending_manager.importSms(sms_data_in_string);

					activity.updateWidget();

					activity.setSetting('current_segment', 'history');
					PUSH({url: 'history.html'});
				}
			);
			var UpdateImportButton = function() {
				if ($('.sms-list .import-flag.active').length) {
					import_button.show();
				} else {
					import_button.hide();
				}
			};

			$('.sms-list').click(
				function() {
					setTimeout(UpdateImportButton, UPDATE_INPUT_BUTTON_TIMEOUT);
				}
			);
			UpdateImportButton();
		}

		window.addEventListener(
			'push',
			function(event) {
				if (/\bhistory\b/.test(event.detail.state.url)) {
					activity.setSetting('current_page', 'history');
					UpdateIndexPage();
				} else if (/\beditor\b/.test(event.detail.state.url)) {
					activity.setSetting('current_page', 'editor');
					UpdateEditorPage();
				} else if (/\bbuy_editor\b/.test(event.detail.state.url)) {
					activity.setSetting('current_page', 'buy_editor');
					UpdateBuyEditorPage();
				} else if (/\bsms\b/.test(event.detail.state.url)) {
					activity.setSetting('current_page', 'sms');
					UpdateSmsPage();
				} else if (/\bauthors\b/.test(event.detail.state.url)) {
					activity.setSetting('current_page', 'authors');
				} else {
					activity.setSetting('current_page', 'history');
				}
			}
		);
		PUSH({url: activity.getSetting('current_page') + '.html'});
	}
);
