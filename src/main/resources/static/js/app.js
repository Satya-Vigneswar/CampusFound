
const searchInput = document.getElementById("searchInput");
const filterButtons = document.querySelectorAll(".filter");
const itemCards = document.querySelectorAll(".item-card");
const emptyMessage = document.getElementById("emptyMessage");

let selectedFilter = "all";

function filterItems() {
    const searchText = searchInput.value.toLowerCase();
    let visibleCount = 0;

    itemCards.forEach(function (card) {
        const itemText = card.textContent.toLowerCase();
        const itemType = card.dataset.type;

        const matchesSearch = itemText.includes(searchText);
        const matchesFilter =
            selectedFilter === "all" || itemType === selectedFilter;

        if (matchesSearch && matchesFilter) {
            card.style.display = "";
            visibleCount++;
        } else {
            card.style.display = "none";
        }
    });

    emptyMessage.hidden = visibleCount > 0;
}

searchInput.addEventListener("input", filterItems);

filterButtons.forEach(function (button) {
    button.addEventListener("click", function () {
        selectedFilter = button.dataset.filter;

        filterButtons.forEach(function (otherButton) {
            otherButton.classList.remove("active");
        });

        button.classList.add("active");
        filterItems();
    });
});
