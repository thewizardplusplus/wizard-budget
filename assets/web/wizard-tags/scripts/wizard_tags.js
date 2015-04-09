/* The MIT License (MIT)
 *
 * Copyright (c) 2015 thewizardplusplus <thewizardplusplus@yandex.ru>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

var WizardTags = (function() {
	var OptionsProcessor = (function() {
		var AscTagsSorter = function(tag_1, tag_2) {
			return tag_1.localeCompare(tag_2);
		};
		var DescTagsSorter = function(tag_1, tag_2) {
			return tag_2.localeCompare(tag_1);
		};
		var GetTagsSorter = function(options) {
			var tags_sorter = null;
			if (
				typeof options.sort == 'string'
				|| options.sort instanceof String
			) {
				if (options.sort == 'asc') {
					tags_sorter = AscTagsSorter;
				} else if (options.sort == 'desc') {
					tags_sorter = DescTagsSorter;
				}
			} else if (options.sort instanceof Function) {
				tags_sorter = options.sort;
			}

			return tags_sorter;
		};
		var MakeDefaultTagsGenerator = function(tags) {
			return function(query) {
				return tags.filter(
					function(tag) {
						return tag.substr(0, query.length) == query;
					}
				);
			};
		};
		var TrimTags = function(tags) {
			return tags.map(
				function(tag) {
					return tag.trim();
				}
			);
		};
		var UniqueTags = function(tags) {
			return tags.filter(
				function(value, index, self) {
					return self.indexOf(value) == index;
				}
			);
		};
		var SortTags = function(tags, sorter) {
			if (sorter) {
				tags = tags.sort(sorter);
			}

			return tags;
		};
		var GetTagsGenerator = function(options) {
			var tags_generator = function() {
				return [];
			};
			if (options.tags instanceof Function) {
				tags_generator = options.tags;
			} else if (options.tags instanceof Array) {
				tags_generator = MakeDefaultTagsGenerator(options.tags);
			}

			return function(query) {
				var tags = tags_generator(query);
				tags = TrimTags(tags);
				tags = UniqueTags(tags);
				tags = SortTags(tags, options.sort);

				return tags;
			};
		};

		return {
			process: function(options) {
				var processed_options = options || {};
				processed_options.sort = GetTagsSorter(processed_options);
				processed_options.tags = GetTagsGenerator(processed_options);
				processed_options.default_tags =
					processed_options.default_tags
					|| [];
				processed_options.separators =
					processed_options.separators
					|| ' ';
				processed_options.only_unique = !!processed_options.only_unique;
				processed_options.placeholder =
					processed_options.placeholder
					|| 'Tags';
				processed_options.onChange =
					processed_options.onChange
					|| function() {};

				return processed_options;
			}
		};
	})();
	var ContainersManager = (function() {
		return {
			getRoot: function(root_element_query) {
				var root_container = document.querySelector(root_element_query);
				root_container.className = 'wizard-tags';

				return root_container;
			},
			makeInner: function(root_container, event_handlers) {
				var inner_container = document.createElement('div');
				inner_container.className = 'inner-container';
				inner_container.addEventListener(
					'click',
					event_handlers.updateAutocompleteList
				);

				root_container.appendChild(inner_container);
				return inner_container;
			}
		};
	})();
	var MakeInput = (function() {
		var LIST_UPDATE_TIMEOUT = 300;
		var LIST_REMOVE_DELAY = 250;

		var UpdateInputSize = function(input) {
			var new_size = input.value.length;
			if (new_size == 0 && input.hasAttribute('placeholder')) {
				var placeholder = input.getAttribute('placeholder');
				new_size = placeholder.length;
			}
			new_size += 1;

			input.setAttribute('size', new_size);
		};
		var ClearInput = function(input) {
			input.value = '';
			UpdateInputSize(input);
		};

		return function(
			inner_container,
			separators,
			placeholder,
			event_handlers
		) {
			var input = document.createElement('input');
			input.className = 'input';
			input.setAttribute('placeholder', placeholder);
			UpdateInputSize(input);

			input.addEventListener(
				'focus',
				function() {
					event_handlers.updateAutocompleteList(this.value);
				}
			);

			var list_update_timer = null;
			input.addEventListener(
				'keyup',
				function(event) {
					var last_symbol = this.value.slice(-1);
					if (
						event.which == 13
						|| (last_symbol.length
						&& separators.indexOf(last_symbol) != -1)
					) {
						event_handlers.addTag(
							event.which == 13
								? this.value
								: this.value.slice(0, -1)
						);

						return;
					}

					UpdateInputSize(this);

					clearTimeout(list_update_timer);
					var self = this;
					list_update_timer = setTimeout(
						function() {
							event_handlers.updateAutocompleteList(self.value);
						},
						LIST_UPDATE_TIMEOUT
					);
				}
			);
			input.addEventListener(
				'keydown',
				function(event) {
					if (event.which == 13) {
						event.preventDefault();
						return false;
					}
				}
			);

			input.addEventListener(
				'blur',
				function() {
					setTimeout(
						function() {
							event_handlers.removeAutocompleteList();
						},
						LIST_REMOVE_DELAY
					);
				}
			);

			input.updateSize = function() {
				UpdateInputSize(this);
			};
			input.clear = function() {
				ClearInput(this);
				event_handlers.updateAutocompleteList('');
			};

			inner_container.appendChild(input);
			return input;
		};
	})();
	var TagManager = function(inner_container, input, event_handlers) {
		var MakeTagView = function() {
			var tag_view = document.createElement('span');
			tag_view.className = 'tag-view';

			return tag_view;
		};
		var MakeTextView = function(text) {
			var text_view = document.createElement('span');
			text_view.className = 'text-view';
			text_view.innerHTML = text;

			return text_view;
		};
		var RemoveTag = function(inner_container, tag_view, event_handlers) {
			inner_container.removeChild(tag_view);
			event_handlers.onTagListChange();
		};
		var MakeRemoveButton = function(
			inner_container,
			tag_view,
			event_handlers
		) {
			var remove_button = document.createElement('span');
			remove_button.className = 'remove-button';
			remove_button.addEventListener(
				'click',
				function() {
					RemoveTag(inner_container, tag_view, event_handlers);
				}
			);

			return remove_button;
		};
		var MakeTag = function(text, inner_container, event_handlers) {
			var tag_view = MakeTagView();

			var text_view = MakeTextView(text);
			tag_view.appendChild(text_view);

			var remove_button = MakeRemoveButton(
				inner_container,
				tag_view,
				event_handlers
			);
			tag_view.appendChild(remove_button);

			return tag_view;
		};

		this.getTags = function() {
			var tags = [];
			var tags_views = inner_container.querySelectorAll('.tag-view');
			for (var i = 0; i < tags_views.length; i++) {
				var tag = tags_views[i].querySelector('.text-view').innerHTML;
				tags.push(tag);
			}

			return tags;
		};
		this.addTag = function(
			text,
			only_unique,
			inner_container,
			input,
			event_handlers
		) {
			text = text.trim();
			if (text.length == 0) {
				return;
			}
			if (only_unique && this.getTags().indexOf(text) != -1) {
				return;
			}

			var tag = MakeTag(text, inner_container, event_handlers);
			inner_container.insertBefore(tag, input);
			event_handlers.onTagListChange();

			input.clear();
		};
	};
	var AutocompleteListManager = function(
		tags_generator,
		root_container,
		event_handlers
	) {
		var MakeListContainer = function() {
			var list = document.createElement('ul');
			list.className = 'autocomplete-list';

			return list;
		};
		var MakeListItem = function(text) {
			text = text.trim();
			if (text.length == 0) {
				return false;
			}

			var item = document.createElement('li');
			item.innerHTML = text;
			item.addEventListener(
				'click',
				function() {
					event_handlers.addTag(text);
				}
			);

			return item;
		};
		var MakeAutocompleteList = function(tags) {
			var list = MakeListContainer();
			for (var i = 0; i < tags.length; i++) {
				var item = MakeListItem(tags[i]);
				if (item) {
					list.appendChild(item);
				}
			}

			return list;
		};

		this.makeList = function(query, additional_filter) {
			var tags = tags_generator(query);
			if (additional_filter) {
				tags = additional_filter(tags);
			}

			if (tags.length) {
				var list = MakeAutocompleteList(tags);
				root_container.appendChild(list);

				return list;
			} else {
				return null;
			}
		};
		this.removeList = function(list) {
			if (list && list.parentNode == root_container) {
				root_container.removeChild(list);
			}
		};
		this.updateList = function(old_list, new_query, additional_filter) {
			this.removeList(old_list);
			return this.makeList(new_query, additional_filter);
		};
	};

	return function(root_element_query, options) {
		options = OptionsProcessor.process(options);

		var self = this;
		var list = null;
		var uniqueFilter = function(tags) {
			return tags.filter(
				function(tag) {
					return tag_manager.getTags().indexOf(tag) == -1;
				}
			);
		};
		var updateAutocompleteList = function(query) {
			list = list_manager.updateList(
				list,
				query,
				options.only_unique ? uniqueFilter : null
			);
		};

		var root_container = ContainersManager.getRoot(root_element_query);
		var inner_container = ContainersManager.makeInner(
			root_container,
			{
				updateAutocompleteList: function() {
					input.focus();
				}
			}
		);

		var tags_event_handlers = {
			onTagListChange: function() {
				input.setAttribute(
					'placeholder',
					self.getTags().length == 0
						? options.placeholder
						: ''
				);
				input.updateSize();

				options.onChange.apply(self);
			}
		};
		var tag_manager = new TagManager(
			inner_container,
			input,
			tags_event_handlers
		);

		var input = MakeInput(
			inner_container,
			options.separators,
			options.placeholder,
			{
				addTag: function(text) {
					tag_manager.addTag(
						text,
						options.only_unique,
						inner_container,
						input,
						tags_event_handlers
					);
				},
				updateAutocompleteList: updateAutocompleteList,
				removeAutocompleteList: function() {
					list_manager.removeList(list);
					list = null;
				}
			}
		);

		var list_manager = new AutocompleteListManager(
			options.tags,
			root_container,
			{
				addTag: function(text) {
					tag_manager.addTag(
						text,
						options.only_unique,
						inner_container,
						input,
						tags_event_handlers
					);
				}
			}
		);

		this.getTags = function() {
			return tag_manager.getTags();
		};

		options.default_tags.map(
			function(default_tag) {
				default_tag = default_tag.trim();
				if (default_tag.length == 0) {
					return;
				}

				tag_manager.addTag(
					default_tag,
					options.only_unique,
					inner_container,
					input,
					tags_event_handlers
				);
			}
		);
		list_manager.removeList(list);
		list = null;
	};
})();
