$(document).ready(
	function() {
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
						    + '<span class = "media-object pull-left">'
							    + '<i class = "fa fa-'
							        + (spending.amount > 0
								        ? 'shopping-cart'
									    : 'money')
									+ ' fa-2x"></i>'
							    + '</span>'
							+ '<div class = "media-body">'
							    + '<p><strong>' + spending.date + ':</strong></p>'
				                + '<p>'
							        + spending.amount + ' <i class = "fa fa-ruble"></i>'
								    + (spending.comment.length
								        ? ' &mdash; <em>' + spending.comment + '</em>'
									    : '')
							    + '.</p>'
							+ '</div>'
						+ '</li>'
					);
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

