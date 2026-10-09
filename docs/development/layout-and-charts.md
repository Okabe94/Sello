# Screen frame, categories and charts

Contracts of the `:design-system` structure components (SELLO-009). They hold no state
and no financial rule; the application supplies selection, the month, formatted exact
amounts and every word.

## Frame and navigation

- `SelloScaffold` arranges itself from the size it is given, through
  `selloWindowLayout(width, height)`:

  | Window | Navigation | Layout |
  | --- | --- | --- |
  | Under 600dp wide, at least 480dp tall | Tab bar and dock under the content | One column |
  | Under 480dp tall, any width | Rail; its "+" replaces the dock | Two even panes |
  | 600–839dp wide | Tab bar and dock | One column capped at 560dp, centred |
  | 840dp and wider | Rail with "+" | 400dp list pane, detail in the rest; side panels 420dp |

- The dock and tab bar are laid out under the content, never over it, and hide while
  the system keyboard is open. The paddings handed to `content` and `secondaryPane`
  already clear the system bars; apply them inside each pane's own scroll container.
- Two panes appear only when the app passes `secondaryPane`. Each pane scrolls alone.
- `SelloNavigation` takes any list of destinations (two in the MVP) and reports every
  selection, including the current tab again; the app owns back stacks and what a
  reselect does. `SelloDock` is the same action for the dock and for the rail's "+".
- `MonthSwitcher` is a title that reports a tap. `MonthGrid` draws the cells it is
  given; the app decides which months are enabled (never a future one), have data or
  are current, using `EffectiveDates.forSelection`.

## Categories

- A category is known by its icon and name in brand ink. There is no per-category
  colour in any API.
- `CategoryCircle` clamps only what it draws. `CategoryCell` shows the exact amount
  the app formatted and speaks the full `description` the app wrote, so an overspend
  far beyond the limit still reads exactly. `CategoryGrid` uses two columns, three from
  560dp, one from font scale 1.3.

## Charts

- A `ChartPoint` carries an `id`, a `value` used only for geometry, and `spoken`, the
  mark in words with its exact amount. A selection reports the `id`; the app looks the
  amount up and shows it with `MoneyText` in a `SlipHeading`. Nothing is read back from
  a pixel or a height, so two amounts that draw identically stay distinct.
- Every mark is a selectable item with its `spoken` text, and the chart has a `summary`,
  so charts work with a screen reader, keyboard and Switch Access.
- Charts are at least 130dp tall and take at most one `ChartReference`. A period that
  has not happened is `ChartPeriod.Upcoming` and is drawn as an outline. Short figures
  such as `86 mil` are allowed only as `valueLabel` and in a reference label.
