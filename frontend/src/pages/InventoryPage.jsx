import {
    useCallback,
    useEffect,
    useMemo,
    useState
} from "react";

import toast from "react-hot-toast";

import {
    Archive,
    Boxes,
    ChevronDown,
    ChevronRight,
    CircleDollarSign,
    Folder,
    FolderOpen,
    Merge,
    PackagePlus,
    Pencil,
    Plus,
    RotateCcw,
    Search,
    Tags,
    Trash2,
    Undo2
} from "lucide-react";

import {
    apiErrorMessage
} from "../api/http";

import {
    inventoryApi,
    pageContent
} from "../api/kioskApi";

import {
    useRealtimeInvalidation
} from "../realtime/useRealtimeInvalidation";

import {
    Button,
    Card,
    EmptyState,
    Input,
    LoadingScreen,
    Modal,
    Money,
    PageHeader,
    SectionTitle,
    StatusBadge,
    formatDateTime
} from "../components/ui";

const EMPTY_ITEM_FORM = {
    name: "",
    categoryId: "",
    categoryName: "",
    price: ""
};

export default function InventoryPage() {
    const [
        categories,
        setCategories
    ] = useState(null);

    const [
        itemsResponse,
        setItemsResponse
    ] = useState(null);

    const [
        transactionsResponse,
        setTransactionsResponse
    ] = useState(null);

    const [
        search,
        setSearch
    ] = useState("");

    const [
        openCategories,
        setOpenCategories
    ] = useState(
        new Set()
    );

    const [
        categoryDialogOpen,
        setCategoryDialogOpen
    ] = useState(false);

    const [
        itemDialogOpen,
        setItemDialogOpen
    ] = useState(false);

    const [
        categoryName,
        setCategoryName
    ] = useState("");

    const [
        itemForm,
        setItemForm
    ] = useState(
        EMPTY_ITEM_FORM
    );

    const [
        itemAction,
        setItemAction
    ] = useState(null);

    const load =
        useCallback(
            async () => {
                try {
                    const [
                        categoryResult,
                        itemResult,
                        transactionResult
                    ] = await Promise.all([
                        inventoryApi.categories(
                            true
                        ),

                        inventoryApi.items({
                            includeArchived: true,
                            page: 0,
                            size: 500
                        }),

                        inventoryApi.transactions({
                            page: 0,
                            size: 50
                        })
                    ]);

                    setCategories(
                        categoryResult
                    );

                    setItemsResponse(
                        itemResult
                    );

                    setTransactionsResponse(
                        transactionResult
                    );

                } catch (error) {
                    toast.error(
                        apiErrorMessage(error)
                    );
                }
            },
            []
        );

    useEffect(
        () => {
            load();
        },
        [load]
    );

    useRealtimeInvalidation({
        privateChannel: true,

        topics: [
            "/topic/inventory"
        ],

        reload: load
    });

    async function execute(
        operation,
        message
    ) {
        try {
            await operation();

            toast.success(message);

            await load();

            return true;

        } catch (error) {
            toast.error(
                apiErrorMessage(error)
            );

            return false;
        }
    }

    async function createCategory(
        event
    ) {
        event.preventDefault();

        if (
            !categoryName.trim()
        ) {
            toast.error(
                "Category name is required."
            );

            return;
        }

        const succeeded =
            await execute(
                () =>
                    inventoryApi
                        .createCategory(
                            categoryName.trim()
                        ),

                "Category created."
            );

        if (succeeded) {
            setCategoryName("");
            setCategoryDialogOpen(false);
        }
    }

    async function createItem(
        event
    ) {
        event.preventDefault();

        if (
            !itemForm.name.trim()
            || itemForm.price === ""
        ) {
            toast.error(
                "Item name and price are required."
            );

            return;
        }

        const request = {
            name:
                itemForm.name.trim(),

            price:
                Number(
                    itemForm.price
                )
        };

        if (
            itemForm.categoryId
        ) {
            request.categoryId =
                Number(
                    itemForm.categoryId
                );

        } else if (
            itemForm.categoryName.trim()
        ) {
            request.categoryName =
                itemForm.categoryName.trim();

        } else {
            toast.error(
                "Choose an existing category or enter a new category name."
            );

            return;
        }

        const succeeded =
            await execute(
                () =>
                    inventoryApi
                        .createItem(
                            request
                        ),

                "Item added to inventory."
            );

        if (succeeded) {
            const categoryId =
                request.categoryId;

            if (categoryId) {
                setOpenCategories(
                    previous => {
                        const next =
                            new Set(previous);

                        next.add(
                            categoryId
                        );

                        return next;
                    }
                );
            }

            setItemForm(
                EMPTY_ITEM_FORM
            );

            setItemDialogOpen(false);
        }
    }

    const allItems =
        pageContent(
            itemsResponse
        );

    const transactions =
        pageContent(
            transactionsResponse
        );

    const itemsByCategory =
        useMemo(
            () => {
                const grouped =
                    new Map();

                allItems.forEach(
                    item => {
                        const key =
                            Number(
                                item.categoryId
                            );

                        if (
                            !grouped.has(key)
                        ) {
                            grouped.set(
                                key,
                                []
                            );
                        }

                        grouped
                            .get(key)
                            .push(item);
                    }
                );

                grouped.forEach(
                    values =>
                        values.sort(
                            (
                                first,
                                second
                            ) =>
                                first.name.localeCompare(
                                    second.name
                                )
                        )
                );

                return grouped;
            },
            [
                allItems
            ]
        );

    const visibleCategories =
        useMemo(
            () => {
                if (!categories) {
                    return [];
                }

                const normalizedSearch =
                    search
                        .trim()
                        .toLowerCase();

                return categories
                    .filter(
                        category =>
                            !category.deleted
                    )
                    .filter(
                        category => {
                            if (
                                !normalizedSearch
                            ) {
                                return true;
                            }

                            if (
                                category.name
                                    .toLowerCase()
                                    .includes(
                                        normalizedSearch
                                    )
                            ) {
                                return true;
                            }

                            return (
                                itemsByCategory
                                    .get(
                                        Number(
                                            category.id
                                        )
                                    )
                                || []
                            ).some(
                                item =>
                                    item.name
                                        .toLowerCase()
                                        .includes(
                                            normalizedSearch
                                        )
                            );
                        }
                    );
            },
            [
                categories,
                itemsByCategory,
                search
            ]
        );

    useEffect(
        () => {
            if (
                !search.trim()
            ) {
                return;
            }

            setOpenCategories(
                previous => {
                    const next =
                        new Set(previous);

                    visibleCategories
                        .forEach(
                            category =>
                                next.add(
                                    Number(
                                        category.id
                                    )
                                )
                        );

                    return next;
                }
            );
        },
        [
            search,
            visibleCategories
        ]
    );

    function toggleCategory(
        categoryId
    ) {
        const numericId =
            Number(categoryId);

        setOpenCategories(
            previous => {
                const next =
                    new Set(previous);

                if (
                    next.has(
                        numericId
                    )
                ) {
                    next.delete(
                        numericId
                    );
                } else {
                    next.add(
                        numericId
                    );
                }

                return next;
            }
        );
    }

    function openAddItemForCategory(
        category
    ) {
        setItemForm({
            ...EMPTY_ITEM_FORM,

            categoryId:
                String(category.id)
        });

        setItemDialogOpen(true);
    }

    if (
        !categories
        || !itemsResponse
        || !transactionsResponse
    ) {
        return (
            <LoadingScreen
                message="Loading inventory..."
            />
        );
    }

    return (
        <div className="page-container">
            <PageHeader
                title="Inventory"
                description="Categories work like folders. Open a category to manage the items stored inside it."
                actions={
                    <>
                        <Button
                            variant="outline"
                            onClick={
                                () =>
                                    setCategoryDialogOpen(
                                        true
                                    )
                            }
                        >
                            <Tags className="h-4 w-4"/>
                            Add Category
                        </Button>

                        <Button
                            onClick={
                                () => {
                                    setItemForm(
                                        EMPTY_ITEM_FORM
                                    );

                                    setItemDialogOpen(
                                        true
                                    );
                                }
                            }
                        >
                            <PackagePlus className="h-4 w-4"/>
                            Add Item
                        </Button>
                    </>
                }
            />

            <div className="mb-6 grid gap-4 sm:grid-cols-3">
                <InventorySummary
                    icon={
                        <Folder className="h-5 w-5"/>
                    }
                    label="Categories"
                    value={
                        categories.filter(
                            category =>
                                !category.deleted
                        ).length
                    }
                />

                <InventorySummary
                    icon={
                        <Boxes className="h-5 w-5"/>
                    }
                    label="Items"
                    value={
                        allItems.filter(
                            item =>
                                !item.archived
                        ).length
                    }
                />

                <InventorySummary
                    icon={
                        <CircleDollarSign className="h-5 w-5"/>
                    }
                    label="Tracked Net Revenue"
                    value={
                        <Money
                            value={
                                allItems.reduce(
                                    (
                                        sum,
                                        item
                                    ) =>
                                        sum
                                        + Number(
                                            item.netRevenue
                                            || 0
                                        ),
                                    0
                                )
                            }
                        />
                    }
                />
            </div>

            <div className="mb-6 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
                <div className="relative">
                    <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"/>

                    <input
                        className="form-control pl-10"
                        placeholder="Search categories or items..."
                        value={search}
                        onChange={
                            event =>
                                setSearch(
                                    event.target.value
                                )
                        }
                    />
                </div>
            </div>

            <SectionTitle
                title="Category Folders"
                description="Click a category to open it and view its items."
            />

            {visibleCategories.length === 0 ? (
                <EmptyState
                    title="No inventory categories found"
                    description={
                        search
                            ? "No category or item matches your search."
                            : "Create your first category and add an item."
                    }
                    action={
                        <Button
                            onClick={
                                () =>
                                    setCategoryDialogOpen(
                                        true
                                    )
                            }
                        >
                            Add Category
                        </Button>
                    }
                />
            ) : (
                <div className="space-y-4">
                    {visibleCategories.map(
                        category => (
                            <CategoryFolder
                                key={category.id}
                                category={category}
                                items={
                                    itemsByCategory.get(
                                        Number(
                                            category.id
                                        )
                                    )
                                    || []
                                }
                                open={
                                    openCategories.has(
                                        Number(
                                            category.id
                                        )
                                    )
                                }
                                onToggle={
                                    () =>
                                        toggleCategory(
                                            category.id
                                        )
                                }
                                onAddItem={
                                    () =>
                                        openAddItemForCategory(
                                            category
                                        )
                                }
                                onItemAction={
                                    setItemAction
                                }
                                execute={execute}
                            />
                        )
                    )}
                </div>
            )}

            <div className="mt-12">
                <SectionTitle
                    title="Recent Inventory Activity"
                    description="Every sale, return and correction is permanently recorded as a transaction."
                />

                {transactions.length === 0 ? (
                    <EmptyState
                        title="No inventory transactions yet"
                    />
                ) : (
                    <div className="professional-scrollbar overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm">
                        <table className="min-w-full text-sm">
                            <thead className="bg-slate-50">
                            <tr className="text-left text-xs font-bold uppercase tracking-[0.08em] text-slate-500">
                                <th className="px-4 py-3">
                                    Time
                                </th>

                                <th className="px-4 py-3">
                                    Item
                                </th>

                                <th className="px-4 py-3">
                                    Type
                                </th>

                                <th className="px-4 py-3">
                                    Qty
                                </th>

                                <th className="px-4 py-3">
                                    Unit Price
                                </th>

                                <th className="px-4 py-3">
                                    Total
                                </th>

                                <th className="px-4 py-3">
                                    Agent
                                </th>
                            </tr>
                            </thead>

                            <tbody className="divide-y divide-slate-100">
                            {transactions.map(
                                transaction => (
                                    <tr
                                        key={
                                            transaction.id
                                        }
                                        className="transition hover:bg-slate-50"
                                    >
                                        <td className="whitespace-nowrap px-4 py-3 text-xs text-slate-500">
                                            {formatDateTime(
                                                transaction.transactedAt
                                            )}
                                        </td>

                                        <td className="px-4 py-3">
                                            <strong className="text-slate-900">
                                                {transaction.itemName}
                                            </strong>

                                            <div className="mt-0.5 text-xs text-slate-500">
                                                {transaction.categoryNameAtTransaction}
                                            </div>
                                        </td>

                                        <td className="px-4 py-3">
                                            <StatusBadge
                                                value={
                                                    transaction.transactionType
                                                }
                                            />
                                        </td>

                                        <td className="px-4 py-3 font-bold">
                                            {transaction.quantity}
                                        </td>

                                        <td className="whitespace-nowrap px-4 py-3">
                                            <Money
                                                value={
                                                    transaction.unitPrice
                                                }
                                            />
                                        </td>

                                        <td className="whitespace-nowrap px-4 py-3 font-semibold">
                                            <Money
                                                value={
                                                    transaction.totalAmount
                                                }
                                            />
                                        </td>

                                        <td className="px-4 py-3">
                                            {transaction.agentId}
                                        </td>
                                    </tr>
                                )
                            )}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            <AddCategoryDialog
                open={categoryDialogOpen}
                value={categoryName}
                onChange={setCategoryName}
                onClose={
                    () => {
                        setCategoryDialogOpen(
                            false
                        );

                        setCategoryName("");
                    }
                }
                onSubmit={createCategory}
            />

            <AddItemDialog
                open={itemDialogOpen}
                categories={
                    categories.filter(
                        category =>
                            !category.archived
                            && !category.deleted
                    )
                }
                form={itemForm}
                setForm={setItemForm}
                onClose={
                    () => {
                        setItemDialogOpen(
                            false
                        );

                        setItemForm(
                            EMPTY_ITEM_FORM
                        );
                    }
                }
                onSubmit={createItem}
            />

            <ItemActionDialog
                action={itemAction}
                onClose={
                    () =>
                        setItemAction(null)
                }
                execute={execute}
            />
        </div>
    );
}

function CategoryFolder({
                            category,
                            items,
                            open,
                            onToggle,
                            onAddItem,
                            onItemAction,
                            execute
                        }) {
    const activeItems =
        items.filter(
            item =>
                !item.archived
        );

    const categoryRevenue =
        items.reduce(
            (
                sum,
                item
            ) =>
                sum
                + Number(
                    item.netRevenue
                    || 0
                ),
            0
        );

    async function renameCategory(
        event
    ) {
        event.stopPropagation();

        const name =
            window.prompt(
                "New category name:",
                category.name
            );

        if (
            !name?.trim()
        ) {
            return;
        }

        await execute(
            () =>
                inventoryApi
                    .renameCategory(
                        category.id,
                        name.trim()
                    ),

            "Category renamed."
        );
    }

    async function toggleArchive(
        event
    ) {
        event.stopPropagation();

        if (
            category.archived
        ) {
            await execute(
                () =>
                    inventoryApi
                        .reactivateCategory(
                            category.id
                        ),

                "Category reactivated. Reactivate required items individually."
            );

        } else {
            const confirmed =
                window.confirm(
                    `Archive "${category.name}" and all items inside it?`
                );

            if (!confirmed) {
                return;
            }

            await execute(
                () =>
                    inventoryApi
                        .archiveCategory(
                            category.id
                        ),

                "Category and its items archived."
            );
        }
    }

    async function mergeCategory(
        event
    ) {
        event.stopPropagation();

        const destination =
            window.prompt(
                "Enter destination category ID:"
            );

        if (
            !destination
            || !Number(destination)
        ) {
            return;
        }

        await execute(
            () =>
                inventoryApi
                    .mergeCategory(
                        category.id,
                        Number(
                            destination
                        )
                    ),

            "Categories merged."
        );
    }

    async function deleteCategory(
        event
    ) {
        event.stopPropagation();

        if (
            !window.confirm(
                `Delete category "${category.name}"? It can only be deleted when no active items remain. Historical transactions will be preserved.`
            )
        ) {
            return;
        }

        await execute(
            () =>
                inventoryApi
                    .deleteCategory(
                        category.id
                    ),

            "Category deleted."
        );
    }

    return (
        <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition duration-200 hover:shadow-md">
            <button
                type="button"
                onClick={onToggle}
                className="flex w-full items-center gap-4 px-4 py-4 text-left sm:px-5"
            >
                <div className={`
            grid h-12 w-12 shrink-0 place-items-center rounded-2xl
            ${
                    category.archived
                        ? "bg-slate-100 text-slate-500"
                        : "bg-blue-50 text-blue-600"
                }
          `}>
                    {open ? (
                        <FolderOpen className="h-6 w-6"/>
                    ) : (
                        <Folder className="h-6 w-6"/>
                    )}
                </div>

                <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                        <h3 className="truncate text-base font-extrabold text-slate-950 sm:text-lg">
                            {category.name}
                        </h3>

                        {category.archived && (
                            <StatusBadge
                                value="ARCHIVED"
                            />
                        )}
                    </div>

                    <div className="mt-1 flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-500">
              <span>
                {activeItems.length}
                  {" active "}
                  {activeItems.length === 1
                      ? "item"
                      : "items"}
              </span>

                        <span>
                {items.length}
                            {" total"}
              </span>

                        <span className="font-semibold text-slate-700">
                <Money
                    value={
                        categoryRevenue
                    }
                />
                            {" net"}
              </span>
                    </div>
                </div>

                <div className="shrink-0 text-slate-400">
                    {open ? (
                        <ChevronDown className="h-5 w-5"/>
                    ) : (
                        <ChevronRight className="h-5 w-5"/>
                    )}
                </div>
            </button>

            {open && (
                <div className="border-t border-slate-100 bg-slate-50/55">
                    <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-4 py-3 sm:px-5">
                        <div className="flex flex-wrap gap-2">
                            {!category.archived && (
                                <Button
                                    variant="outline"
                                    className="min-h-9 px-3 py-1.5 text-xs"
                                    onClick={onAddItem}
                                >
                                    <Plus className="h-3.5 w-3.5"/>
                                    Add Item Here
                                </Button>
                            )}

                            <Button
                                variant="ghost"
                                className="min-h-9 px-3 py-1.5 text-xs"
                                onClick={renameCategory}
                            >
                                <Pencil className="h-3.5 w-3.5"/>
                                Rename
                            </Button>

                            <Button
                                variant="ghost"
                                className="min-h-9 px-3 py-1.5 text-xs"
                                onClick={toggleArchive}
                            >
                                {category.archived ? (
                                    <RotateCcw className="h-3.5 w-3.5"/>
                                ) : (
                                    <Archive className="h-3.5 w-3.5"/>
                                )}

                                {category.archived
                                    ? "Reactivate"
                                    : "Archive"}
                            </Button>

                            <Button
                                variant="ghost"
                                className="min-h-9 px-3 py-1.5 text-xs"
                                onClick={mergeCategory}
                            >
                                <Merge className="h-3.5 w-3.5"/>
                                Merge
                            </Button>

                            <Button
                                variant="ghost"
                                className="min-h-9 px-3 py-1.5 text-xs text-red-600 hover:bg-red-50 hover:text-red-700"
                                onClick={deleteCategory}
                            >
                                <Trash2 className="h-3.5 w-3.5"/>
                                Delete
                            </Button>
                        </div>
                    </div>

                    <div className="p-4 sm:p-5">
                        {items.length === 0 ? (
                            <EmptyState
                                title="This category is empty"
                                description="Add an item and it will appear inside this folder."
                                action={
                                    !category.archived ? (
                                        <Button
                                            onClick={
                                                onAddItem
                                            }
                                        >
                                            <Plus className="h-4 w-4"/>
                                            Add First Item
                                        </Button>
                                    ) : null
                                }
                            />
                        ) : (
                            <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
                                {items.map(
                                    item => (
                                        <InventoryItemCard
                                            key={item.id}
                                            item={item}
                                            onAction={
                                                type =>
                                                    onItemAction({
                                                        type,
                                                        item
                                                    })
                                            }
                                            execute={execute}
                                        />
                                    )
                                )}
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

function InventoryItemCard({
                               item,
                               onAction,
                               execute
                           }) {
    return (
        <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
            <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                    <h4 className="truncate font-extrabold text-slate-950">
                        {item.name}
                    </h4>

                    <p className="mt-1 text-xs text-slate-500">
                        Item #{item.id}
                    </p>
                </div>

                <StatusBadge
                    value={
                        item.archived
                            ? "ARCHIVED"
                            : "ACTIVE"
                    }
                />
            </div>

            <div className="mt-4 grid grid-cols-2 gap-3 rounded-xl bg-slate-50 p-3">
                <div>
                    <p className="muted-label">
                        Current Price
                    </p>

                    <p className="mt-1 text-sm font-extrabold text-blue-700">
                        <Money
                            value={
                                item.currentPrice
                            }
                        />
                    </p>
                </div>

                <div>
                    <p className="muted-label">
                        Net Sales
                    </p>

                    <p className="mt-1 text-sm font-extrabold text-slate-900">
                        {item.netSoldQuantity ?? 0}
                    </p>
                </div>

                <div className="col-span-2">
                    <p className="muted-label">
                        Net Revenue
                    </p>

                    <p className="mt-1 text-sm font-extrabold text-emerald-700">
                        <Money
                            value={
                                item.netRevenue
                            }
                        />
                    </p>
                </div>
            </div>

            <div className="mt-4 grid grid-cols-2 gap-2">
                <Button
                    variant="success"
                    disabled={
                        item.archived
                    }
                    onClick={
                        () =>
                            execute(
                                () =>
                                    inventoryApi
                                        .sale(
                                            item.id
                                        ),

                                `${item.name}: sale recorded.`
                            )
                    }
                >
                    <Plus className="h-4 w-4"/>
                    +1 Sale
                </Button>

                <Button
                    variant="outline"
                    disabled={
                        item.archived
                    }
                    onClick={
                        () =>
                            onAction(
                                "RETURN"
                            )
                    }
                >
                    <Undo2 className="h-4 w-4"/>
                    Return
                </Button>

                <Button
                    variant="outline"
                    onClick={
                        () =>
                            onAction(
                                "CORRECTION"
                            )
                    }
                >
                    Correction
                </Button>

                <Button
                    variant="outline"
                    onClick={
                        () =>
                            onAction(
                                "PRICE"
                            )
                    }
                >
                    Price
                </Button>

                <Button
                    variant="ghost"
                    onClick={
                        () =>
                            onAction(
                                "RENAME"
                            )
                    }
                >
                    Rename
                </Button>

                {!item.archived ? (
                    <Button
                        variant="ghost"
                        className="text-amber-700"
                        onClick={
                            () => {
                                if (
                                    !window.confirm(
                                        `Archive "${item.name}"?`
                                    )
                                ) {
                                    return;
                                }

                                execute(
                                    () =>
                                        inventoryApi
                                            .archiveItem(
                                                item.id
                                            ),

                                    "Item archived."
                                );
                            }
                        }
                    >
                        Archive
                    </Button>
                ) : (
                    <Button
                        variant="success"
                        onClick={
                            () =>
                                execute(
                                    () =>
                                        inventoryApi
                                            .reactivateItem(
                                                item.id
                                            ),

                                    "Item reactivated."
                                )
                        }
                    >
                        Reactivate
                    </Button>
                )}
            </div>
        </div>
    );
}

function AddCategoryDialog({
                               open,
                               value,
                               onChange,
                               onClose,
                               onSubmit
                           }) {
    return (
        <Modal
            open={open}
            title="Create Category"
            description="Create a parent inventory folder for related items."
            onClose={onClose}
        >
            <form
                onSubmit={onSubmit}
                className="space-y-5"
            >
                <Input
                    label="Category name"
                    value={value}
                    onChange={
                        event =>
                            onChange(
                                event.target.value
                            )
                    }
                    placeholder="Beverage"
                    autoFocus
                />

                <div className="flex justify-end gap-2">
                    <Button
                        variant="outline"
                        onClick={onClose}
                    >
                        Cancel
                    </Button>

                    <Button
                        type="submit"
                    >
                        Create Category
                    </Button>
                </div>
            </form>
        </Modal>
    );
}

function AddItemDialog({
                           open,
                           categories,
                           form,
                           setForm,
                           onClose,
                           onSubmit
                       }) {
    return (
        <Modal
            open={open}
            title="Add Inventory Item"
            description="Place the new item inside an existing category folder or create a new category."
            onClose={onClose}
        >
            <form
                onSubmit={onSubmit}
                className="space-y-4"
            >
                <Input
                    label="Item name"
                    value={form.name}
                    onChange={
                        event =>
                            setForm(
                                previous => ({
                                    ...previous,
                                    name:
                                    event.target.value
                                })
                            )
                    }
                    placeholder="Frutika"
                    autoFocus
                />

                <label className="block">
            <span className="form-label">
              Existing category
            </span>

                    <select
                        className="form-control"
                        value={
                            form.categoryId
                        }
                        onChange={
                            event =>
                                setForm(
                                    previous => ({
                                        ...previous,

                                        categoryId:
                                        event.target.value,

                                        categoryName:
                                            event.target.value
                                                ? ""
                                                : previous.categoryName
                                    })
                                )
                        }
                    >
                        <option value="">
                            Choose a category
                        </option>

                        {categories.map(
                            category => (
                                <option
                                    key={category.id}
                                    value={category.id}
                                >
                                    {category.name}
                                </option>
                            )
                        )}
                    </select>
                </label>

                <div className="relative py-1 text-center">
                    <div className="absolute inset-x-0 top-1/2 border-t border-slate-200"/>

                    <span className="relative bg-white px-3 text-xs font-bold uppercase tracking-wider text-slate-400">
              or
            </span>
                </div>

                <Input
                    label="New category name"
                    placeholder="Stationery"
                    disabled={
                        Boolean(
                            form.categoryId
                        )
                    }
                    value={
                        form.categoryName
                    }
                    onChange={
                        event =>
                            setForm(
                                previous => ({
                                    ...previous,

                                    categoryName:
                                    event.target.value
                                })
                            )
                    }
                />

                <Input
                    label="Price"
                    type="number"
                    step="0.01"
                    min="0"
                    value={
                        form.price
                    }
                    onChange={
                        event =>
                            setForm(
                                previous => ({
                                    ...previous,

                                    price:
                                    event.target.value
                                })
                            )
                    }
                    placeholder="30.00"
                />

                <div className="flex justify-end gap-2 pt-2">
                    <Button
                        variant="outline"
                        onClick={onClose}
                    >
                        Cancel
                    </Button>

                    <Button
                        type="submit"
                    >
                        Add Item
                    </Button>
                </div>
            </form>
        </Modal>
    );
}

function ItemActionDialog({
                              action,
                              onClose,
                              execute
                          }) {
    const [
        value,
        setValue
    ] = useState("");

    const [
        reason,
        setReason
    ] = useState("");

    useEffect(
        () => {
            if (!action) {
                setValue("");
                setReason("");
                return;
            }

            if (
                action.type === "PRICE"
            ) {
                setValue(
                    String(
                        action.item.currentPrice
                        ?? ""
                    )
                );
            }

            if (
                action.type === "RENAME"
            ) {
                setValue(
                    action.item.name
                );
            }
        },
        [
            action
        ]
    );

    if (!action) {
        return null;
    }

    const {
        item,
        type
    } = action;

    const titles = {
        RETURN:
            "Record Product Return",

        CORRECTION:
            "Manual Correction",

        PRICE:
            "Change Item Price",

        RENAME:
            "Rename Item"
    };

    async function submit(
        event
    ) {
        event.preventDefault();

        let succeeded = false;

        if (
            type === "RETURN"
        ) {
            if (!reason.trim()) {
                toast.error(
                    "Return reason is required."
                );

                return;
            }

            succeeded =
                await execute(
                    () =>
                        inventoryApi
                            .returnItem(
                                item.id,
                                reason.trim()
                            ),

                    "Return recorded."
                );
        }

        if (
            type === "CORRECTION"
        ) {
            const quantity =
                Number(value);

            if (
                ![-1, 1]
                    .includes(quantity)
            ) {
                toast.error(
                    "Correction quantity must be 1 or -1."
                );

                return;
            }

            if (!reason.trim()) {
                toast.error(
                    "Correction reason is required."
                );

                return;
            }

            succeeded =
                await execute(
                    () =>
                        inventoryApi
                            .correction(
                                item.id,
                                quantity,
                                reason.trim()
                            ),

                    "Manual correction recorded."
                );
        }

        if (
            type === "PRICE"
        ) {
            const price =
                Number(value);

            if (
                !Number.isFinite(price)
                || price < 0
            ) {
                toast.error(
                    "Enter a valid non-negative price."
                );

                return;
            }

            succeeded =
                await execute(
                    () =>
                        inventoryApi
                            .changePrice(
                                item.id,
                                price
                            ),

                    "Item price updated."
                );
        }

        if (
            type === "RENAME"
        ) {
            if (!value.trim()) {
                toast.error(
                    "Item name is required."
                );

                return;
            }

            succeeded =
                await execute(
                    () =>
                        inventoryApi
                            .renameItem(
                                item.id,
                                value.trim()
                            ),

                    "Item renamed."
                );
        }

        if (succeeded) {
            onClose();
        }
    }

    return (
        <Modal
            open
            title={titles[type]}
            description={item.name}
            onClose={onClose}
        >
            <form
                onSubmit={submit}
                className="space-y-4"
            >
                {type === "CORRECTION" && (
                    <label className="block">
                <span className="form-label">
                  Quantity
                </span>

                        <select
                            className="form-control"
                            value={value}
                            onChange={
                                event =>
                                    setValue(
                                        event.target.value
                                    )
                            }
                        >
                            <option value="">
                                Select
                            </option>

                            <option value="1">
                                +1
                            </option>

                            <option value="-1">
                                -1
                            </option>
                        </select>
                    </label>
                )}

                {type === "PRICE" && (
                    <Input
                        label="New price"
                        type="number"
                        min="0"
                        step="0.01"
                        value={value}
                        onChange={
                            event =>
                                setValue(
                                    event.target.value
                                )
                        }
                    />
                )}

                {type === "RENAME" && (
                    <Input
                        label="Item name"
                        value={value}
                        onChange={
                            event =>
                                setValue(
                                    event.target.value
                                )
                        }
                    />
                )}

                {[
                    "RETURN",
                    "CORRECTION"
                ].includes(type) && (
                    <label className="block">
                <span className="form-label">
                  Reason
                </span>

                        <textarea
                            className="form-control min-h-28 resize-y"
                            value={reason}
                            onChange={
                                event =>
                                    setReason(
                                        event.target.value
                                    )
                            }
                            placeholder="Enter the reason..."
                        />
                    </label>
                )}

                <div className="flex justify-end gap-2 pt-2">
                    <Button
                        variant="outline"
                        onClick={onClose}
                    >
                        Cancel
                    </Button>

                    <Button
                        type="submit"
                    >
                        Save
                    </Button>
                </div>
            </form>
        </Modal>
    );
}

function InventorySummary({
                              icon,
                              label,
                              value
                          }) {
    return (
        <div className="surface-elevated flex items-center gap-4 p-4">
            <div className="grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-blue-50 text-blue-600">
                {icon}
            </div>

            <div className="min-w-0">
                <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                    {label}
                </p>

                <div className="mt-1 truncate text-xl font-extrabold text-slate-950">
                    {value}
                </div>
            </div>
        </div>
    );
}