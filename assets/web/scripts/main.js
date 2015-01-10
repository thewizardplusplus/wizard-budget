$(document).ready(
	function() {
		var active_spending_id = null;

		function HideMainMenu() {
			var event = new CustomEvent('touchend');
			$('.backdrop').get(0).dispatchEvent(event);
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
			var spendings = $.parseJSON(raw_spendings);
			spendings.map(
		        function(spending) {
					spending_list.append(
						'<li class = "table-view-cell media">'
							+ '<button class = "btn second-list-button" data-spending-id = "' + spending.id + '"><i class = "fa fa-pencil"></i></button>'
							+ '<button class = "btn remove-spending-button" data-spending-id = "' + spending.id + '"><i class = "fa fa-trash"></i></button>'
							+ '<span class = "media-object pull-left">'
							    + '<i class = "fa fa-'
							        + (spending.amount > 0
								        ? 'shopping-cart'
									    : 'money')
									+ ' fa-2x"></i>'
							    + '</span>'
							+ '<div class = "media-body">'
								+ '<p><strong><span class = "date-view">' + spending.date + '</span>:</strong></p>'
				                + '<p>'
							        + '<span class = "amount-view">' + spending.amount + '</span> <i class = "fa fa-ruble"></i>'
								    + (spending.comment.length
								        ? ' &mdash; <em><span class = "comment-view">' + spending.comment + '</span></em>'
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
					if ($.type(active_spending_id) !== "null") {
						spending_manager.removeSpending(parseInt(active_spending_id));
						active_spending_id = null;

						PUSH({url: 'history.html'});
					}
				}
			);
			var remove_dialog_date_view = $('.date-view', remove_dialog);
			var remove_dialog_amount_view = $('.amount-view', remove_dialog);
			var remove_dialog_comment_view = $('.comment-view', remove_dialog);

			$('.remove-spending-button', spending_list).click(
				function() {
					var button = $(this);
					active_spending_id = button.data('spending-id');

					var list_item = button.parent();
					var date = $('.date-view', list_item).text();
					var amount = $('.amount-view', list_item).text();
					var comment = $('.comment-view', list_item).text();

					remove_dialog_date_view.text(date);
					remove_dialog_amount_view.text(amount);
					if (comment.length) {
						remove_dialog_comment_view.html(' &mdash; ' + comment);
					}

					remove_dialog.addClass('active');
				}
			);
		}
		function UpdateControlButtons() {
		    $('.remove-last-spending-button').click(
			    function() {
				    spending_manager.removeLastSpending();
				    UpdateSpendingList();
					HideMainMenu();
		        }
		    );
		    $('.backup-button').click(
			    function() {
				    spending_manager.backup();
					HideMainMenu();
		        }
		    );
		}
		function UpdateIndexPage() {
			UpdateControlButtons();
			UpdateSpendingList();
		}
		function UpdateAddPage() {
			var amount_editor = $('.amount-editor');
			amount_editor.focus();
					
			$('.add-spending-button').click(
		        function() {
			        var amount = parseFloat(amount_editor.val());
			    	amount_editor.val('');

				    var comment_editor = $('.comment-editor');
				    var comment = comment_editor.val();
				    comment_editor.val('');

				    spending_manager.addSpending(amount, comment);
				    
					PUSH({url: 'history.html'});
	            }
		    );
		}
		
		window.addEventListener(
		    'push',
		    function(event) {
				if (/\bhistory\b/.test(event.detail.state.url)) {
					UpdateIndexPage();
				} else if (/\badd\b/.test(event.detail.state.url)) {
					UpdateAddPage();
				}
			}
		);
		PUSH({url: 'history.html'});
	}
);

