package com.example.btl_dnc.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.FilterBottomSheet;
import com.example.btl_dnc.R;
import com.example.btl_dnc.adapter.ReportAdapter;
import com.example.btl_dnc.model.Report;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManagerIncidentFragment extends Fragment {

    // Khai báo các view
    private TextView tvMainTitle, tvSubTitle;
    private RecyclerView rvReports;
    private EditText edtSearch;
    private ImageButton btnBackSearch;
    private com.google.android.material.chip.ChipGroup layoutFilters;
    private boolean hasStartedSearching = false;
    private TextView tvListTitle, tvResultCount, tvSort;

    // Khai báo Adapter, List và Firestore
    private ReportAdapter adapter;
    private List<Report> allReportsList;
    private List<Report> filteredReportsList;
    private FirebaseFirestore db;
    private String currentTimeFilter = "";
    private List<String> currentCategoryFilters = new ArrayList<>();
    private List<String> currentStatusFilters = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manager_incident, container, false);

        // Ánh xạ View
        rvReports = view.findViewById(R.id.rvIncidents);
        edtSearch = view.findViewById(R.id.edtSearch);
        btnBackSearch = view.findViewById(R.id.btnBackSearch);
        layoutFilters = view.findViewById(R.id.layoutFilters);
        tvListTitle = view.findViewById(R.id.tvListTitle);
        tvResultCount = view.findViewById(R.id.tvResultCount);
        tvSort = view.findViewById(R.id.tvSort);
        tvMainTitle = view.findViewById(R.id.tvMainTitle);
        tvSubTitle = view.findViewById(R.id.tvSubTitle);
        // Khởi tạo Firestore và dữ liệu
        db = FirebaseFirestore.getInstance();
        allReportsList = new ArrayList<>();
        filteredReportsList = new ArrayList<>();
// Ánh xạ nút
        TextView btnFilterOptions = view.findViewById(R.id.btnFilterOptions);

// Gán sự kiện mở Popup
        if (btnFilterOptions != null) {
            btnFilterOptions.setOnClickListener(v -> {
                FilterBottomSheet bottomSheet = new FilterBottomSheet();
                bottomSheet.setInitialFilters(currentTimeFilter, currentCategoryFilters, currentStatusFilters);
                bottomSheet.setFilterListener((timeFilter, categories, statuses) -> {
                    currentTimeFilter = timeFilter;
                    currentCategoryFilters = categories;
                    currentStatusFilters = statuses;
                    // Cập nhật giao diện (Thêm các khối danh mục bên phải)
                    updateFilterUI();

                    // Lọc lại dữ liệu RecyclerView
                    applyAllFilters();
                });
                bottomSheet.show(getParentFragmentManager(), "FilterBottomSheet");
            });
        }
        // Setup RecyclerView
        rvReports.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReportAdapter(getContext(), filteredReportsList, "ADMIN");
        rvReports.setAdapter(adapter);

        // Ẩn số lượng kết quả và bộ lọc lúc mới vào
        resetUIState();

        // Xử lý sự kiện tìm kiếm
        setupSearchLogic();

        // Gọi dữ liệu từ Firebase
        loadDataFromFirebase();

        return view;
    }

    private void setupSearchLogic() {
        // SỰ KIỆN QUAN TRỌNG: Chạm vào ô tìm kiếm
        edtSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // 1. Đánh dấu là đã bắt đầu tìm kiếm vĩnh viễn
                hasStartedSearching = true;

                // 2. Chạy logic ẩn tiêu đề lớn và hiện thanh Lọc
                updateSearchUIState(edtSearch.getText().toString());

                android.util.Log.d("DEBUG", "Đã chạm vào Search - Ẩn tiêu đề vĩnh viễn");
            }
        });

        // Sự kiện khi gõ chữ
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                filterReports(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Nút quay lại (Mũi tên)
        btnBackSearch.setOnClickListener(v -> {
            edtSearch.setText("");
            edtSearch.clearFocus();
            // Gọi resetUIState để ẩn nút Back, nhưng vì hasStartedSearching = true
            // nên tiêu đề lớn vẫn sẽ bị ẩn vĩnh viễn theo logic trong resetUIState.
            resetUIState();
        });
    }

    private void filterReports(String query) {
        filteredReportsList.clear();

        if (query.isEmpty()) {
            filteredReportsList.addAll(allReportsList);
            resetUIState();
        } else {
            for (Report report : allReportsList) {
                // Kiểm tra null an toàn và tìm kiếm theo nội dung
                if (report.content != null && report.content.toLowerCase().contains(query.toLowerCase())) {
                    filteredReportsList.add(report);
                }
            }
            updateSearchUIState(query);
        }

        adapter.notifyDataSetChanged();
    }

    private void resetUIState() {
        btnBackSearch.setVisibility(View.GONE);
        layoutFilters.setVisibility(View.GONE);
        tvResultCount.setVisibility(View.GONE);
        tvSort.setVisibility(View.GONE);

        // LOGIC ẨN VĨNH VIỄN:
        if (hasStartedSearching) {
            // Nếu đã từng chạm vào search, ẩn luôn tiêu đề
            if (tvMainTitle != null) tvMainTitle.setVisibility(View.GONE);
            if (tvSubTitle != null) tvSubTitle.setVisibility(View.GONE);
        } else {
            // Chỉ hiện khi mới vào App và chưa chạm vào search lần nào
            if (tvMainTitle != null) tvMainTitle.setVisibility(View.VISIBLE);
            if (tvSubTitle != null) tvSubTitle.setVisibility(View.VISIBLE);
        }

        tvListTitle.setText("Báo cáo");
        tvListTitle.setTextColor(getResources().getColor(android.R.color.black));
    }

    private void updateSearchUIState(String query) {
        // Xóa hoặc comment dòng check empty này đi để nút Lọc hiện ra ngay lập tức
        // if (query.isEmpty()) { resetUIState(); return; }
        if (query.trim().isEmpty() && !hasStartedSearching) {
            return;
        }
        hasStartedSearching = true;

        btnBackSearch.setVisibility(View.VISIBLE);
        layoutFilters.setVisibility(View.VISIBLE); // HIỆN CỤM NÚT LỌC

        // Chỉ hiện "X kết quả tìm thấy" khi thực sự có gõ chữ
        if (query.trim().isEmpty()) {
            tvResultCount.setVisibility(View.GONE);
            tvSort.setVisibility(View.GONE);
        } else {
            tvResultCount.setVisibility(View.VISIBLE);
            tvSort.setVisibility(View.VISIBLE);
        }

        if (tvMainTitle != null) tvMainTitle.setVisibility(View.GONE);
        if (tvSubTitle != null) tvSubTitle.setVisibility(View.GONE);

        if (!query.trim().isEmpty() && filteredReportsList.isEmpty()) {
            tvListTitle.setText("Không có kết quả");
            tvListTitle.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            tvListTitle.setText("News");
            tvListTitle.setTextColor(getResources().getColor(android.R.color.black));
            if (!query.trim().isEmpty()) {
                tvResultCount.setText(filteredReportsList.size() + " Kết quả tìm thấy:");
            }
        }
    }

    // ===== HÀM LOAD DỮ LIỆU TỪ FIREBASE =====
    private void loadDataFromFirebase() {

        // 1. Khởi tạo hướng sắp xếp (Mới nhất lên đầu)
        Query.Direction direction = Query.Direction.DESCENDING;

        // 2. Gọi API từ Firestore
        FirebaseFirestore.getInstance()
                .collection("reports")
                // .whereEqualTo("status", "Đang xử lý") // Tạm ẩn bộ lọc này để Manager thấy toàn bộ báo cáo
                .orderBy("createdAt", direction)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    // Xóa dữ liệu cũ trước khi nạp mới để tránh trùng lặp
                    allReportsList.clear();
                    filteredReportsList.clear();

                    // 3. Duyệt qua từng kết quả trả về
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Report r = doc.toObject(Report.class);

                        if (r != null) {
                            r.setId(doc.getId()); // Gán ID của document vào model
                            allReportsList.add(r);
                        }
                    }

                    // 4. Copy dữ liệu sang list hiển thị và cập nhật giao diện
                    filteredReportsList.addAll(allReportsList);
                    adapter.notifyDataSetChanged();

                    // Cập nhật lại các dòng chữ (Ví dụ: "News", "X Kết quả tìm thấy")
                    resetUIState();

                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    // Hiển thị lỗi ra màn hình để dễ debug
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void updateFilterUI() {
        // 1. Xóa các danh mục cũ đã thêm trước đó (Chừa lại vị trí số 0 là nút "Lọc" gốc)
        while (layoutFilters.getChildCount() > 1) {
            layoutFilters.removeViewAt(1);
        }

        // 2. Vẽ thêm khối thời gian (Ví dụ: "Tuần này")
        if (currentTimeFilter != null && !currentTimeFilter.trim().isEmpty()) {
            addFilterChipToLayout(currentTimeFilter);
        }

        // 3. Vẽ thêm các khối danh mục (Điện, Nước...)
        for (String category : currentCategoryFilters) {
            addFilterChipToLayout(category);
        }
        for (String status : currentStatusFilters) {
            addFilterChipToLayout(status);
        }
    }

    // Hàm phụ trợ tạo giao diện cho từng khối
    private void addFilterChipToLayout(String text) {
        TextView chip = new TextView(getContext());
        chip.setText(text);
        chip.setBackgroundResource(R.drawable.bg_chip_outline);

        // Chuyển đổi dp sang px để set Padding cho chuẩn xác (Ngang 16dp, Dọc 8dp)
        float density = getResources().getDisplayMetrics().density;
        int paddingHorizontal = (int) (16 * density);
        int paddingVertical = (int) (8 * density);

        // Set padding giống hệt nút Lọc
        chip.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);

        chip.setTextColor(getResources().getColor(android.R.color.black));
        // Set cỡ chữ 14sp
        chip.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f);

        // Thêm vào ChipGroup
        layoutFilters.addView(chip);
    }
    private void applyAllFilters() {
        String query = edtSearch.getText().toString().trim().toLowerCase();
        filteredReportsList.clear();

        for (Report report : allReportsList) {
            // 1. Điều kiện 1: Khớp chữ tìm kiếm
            boolean matchSearch = query.isEmpty() ||
                    (report.content != null && report.content.toLowerCase().contains(query));

            // 2. Điều kiện 2: Khớp danh mục
            // LƯU Ý: Đảm bảo model Report của bạn có phương thức getCategory() hoặc biến category
            boolean matchCategory = currentCategoryFilters.isEmpty() ||
                    (report.type != null && currentCategoryFilters.contains(report.type));

            // 3. Điều kiện 3: Khớp thời gian (Bạn có thể tự code thêm logic ngày tháng ở đây)
            // boolean matchTime = checkTimeFilter(report.createdAt, currentTimeFilter);
            boolean matchTime = true; // Tạm thời cho phép qua nếu chưa xử lý ngày tháng

            boolean matchStatus = currentStatusFilters.isEmpty() ||
                    (report.status != null && currentStatusFilters.contains(report.status));
            // Nếu thỏa mãn tất cả điều kiện thì mới đưa vào danh sách hiển thị
            if (matchSearch && matchCategory && matchTime && matchStatus) {
                filteredReportsList.add(report);
            }
        }

        adapter.notifyDataSetChanged();

        // Cập nhật lại dòng chữ "X kết quả tìm thấy"
        updateSearchUIState(query);
    }
}