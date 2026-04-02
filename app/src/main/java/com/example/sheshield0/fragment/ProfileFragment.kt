package com.example.sheshield0.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sheshield0.R
import com.example.sheshield0.adapter.FamilyAdapter
import com.example.sheshield0.model.FamilyMember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var rvFamily: RecyclerView
    private lateinit var tvNoFamily: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnEditFamily: Button
    private lateinit var familyAdapter: FamilyAdapter

    private lateinit var userRef: DatabaseReference
    private var userListener: ValueEventListener? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvUserPhone = view.findViewById(R.id.tvUserPhone)
        rvFamily = view.findViewById(R.id.rvFamily)
        tvNoFamily = view.findViewById(R.id.tvNoFamily)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnEditFamily = view.findViewById(R.id.btnEditProfile)

        rvFamily.layoutManager = LinearLayoutManager(requireContext())
        familyAdapter = FamilyAdapter(emptyList())
        rvFamily.adapter = familyAdapter

        btnLogout.setOnClickListener { logoutUser() }
        btnEditFamily.setOnClickListener { chooseFamilyMemberToEdit() }

        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun logoutUser() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ -> performLogout() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        val prefs = requireContext().getSharedPreferences("SheShieldPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_LONG).show()
        val intent = Intent(requireContext(), com.example.sheshield0.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun loadUserData() {
        tvUserName.text = "Loading..."
        tvUserEmail.text = "Loading..."
        tvUserPhone.text = "Loading..."

        userListener?.let { try { userRef.removeEventListener(it) } catch (_: Exception) {} }

        val prefs = requireContext().getSharedPreferences("SheShieldPrefs", Context.MODE_PRIVATE)
        val userMobile = prefs.getString("current_user_mobile", null)

        if (!userMobile.isNullOrEmpty()) {
            loadUserFromDatabase(userMobile)
        } else {
            val currentUser = auth.currentUser
            if (currentUser != null) findUserByEmail(currentUser.email ?: "")
            else showLoginPrompt()
        }
    }

    private fun showLoginPrompt() {
        tvUserName.text = "Please Login"
        tvUserEmail.text = ""
        tvUserPhone.text = ""
        rvFamily.visibility = View.GONE
        tvNoFamily.visibility = View.VISIBLE
        tvNoFamily.text = "Please login to view profile"
    }

    private fun findUserByEmail(email: String) {
        if (email.isEmpty()) return

        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val mobile = userSnapshot.key
                            if (!mobile.isNullOrEmpty()) {
                                val prefs = requireContext().getSharedPreferences("SheShieldPrefs", Context.MODE_PRIVATE)
                                prefs.edit().putString("current_user_mobile", mobile).apply()
                                loadUserFromDatabase(mobile)
                                return
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadUserFromDatabase(userMobile: String) {
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile)

        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || isDetached) return
                if (!snapshot.exists()) return

                tvUserName.text = snapshot.child("name").getValue(String::class.java) ?: "Not set"
                tvUserEmail.text = snapshot.child("email").getValue(String::class.java) ?: "Not set"
                tvUserPhone.text = snapshot.child("mobile").getValue(String::class.java) ?: "Not set"

                loadFamilyMembers(snapshot)
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        userRef.addValueEventListener(userListener!!)
    }

    private fun loadFamilyMembers(userSnapshot: DataSnapshot) {
        val familyList = mutableListOf<FamilyMember>()
        val familySnapshot = userSnapshot.child("family")

        if (familySnapshot.exists()) {
            for (child in familySnapshot.children) {
                child.getValue(FamilyMember::class.java)?.let { familyList.add(it) }
            }
        }

        if (familyList.isEmpty()) {
            rvFamily.visibility = View.GONE
            tvNoFamily.visibility = View.VISIBLE
            tvNoFamily.text = "No family members added"
        } else {
            rvFamily.visibility = View.VISIBLE
            tvNoFamily.visibility = View.GONE
            familyAdapter.updateData(familyList)
        }
    }

    private fun chooseFamilyMemberToEdit() {
        val familyList = familyAdapter.getFamilyList()
        if (familyList.isEmpty()) {
            Toast.makeText(requireContext(), "No family member to edit", Toast.LENGTH_SHORT).show()
            return
        }

        val names = familyList.map { it.name.ifEmpty { "Not set" } }.toTypedArray()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Family Member")
            .setItems(names) { _, which ->
                showEditFamilyDialog(which)
            }
            .show()
    }

    private fun showEditFamilyDialog(index: Int) {
        val familyList = familyAdapter.getFamilyList()
        if (index !in familyList.indices) return

        val member = familyList[index]

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)
        val etRelation = dialogView.findViewById<Spinner>(R.id.spRelation)

        etName.setText(member.name)
        etEmail.setText(member.email)
        etPhone.setText(member.mobile)

        val relations = arrayOf("Select Relation", "Mother", "Father", "Brother", "Sister", "Other")
        etRelation.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, relations)
        val relationPos = relations.indexOf(member.relation)
        etRelation.setSelection(if (relationPos >= 0) relationPos else 0)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Edit Family Member")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val relation = etRelation.selectedItem.toString()

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || relation == "Select Relation") {
                    Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                } else {
                    updateFamilyMember(index, FamilyMember(name, relation, phone, email))
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateFamilyMember(index: Int, updatedMember: FamilyMember) {
        val prefs = requireContext().getSharedPreferences("SheShieldPrefs", Context.MODE_PRIVATE)
        val currentMobile = prefs.getString("current_user_mobile", null) ?: return

        val familyRef = FirebaseDatabase.getInstance().getReference("users")
            .child(currentMobile).child("family")

        val familyList = familyAdapter.getFamilyList().toMutableList()
        if (index in familyList.indices) {
            familyList[index] = updatedMember

            val mapUpdates = mutableMapOf<String, Any>()
            familyList.forEachIndexed { i, member -> mapUpdates[i.toString()] = member }

            familyRef.updateChildren(mapUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Family member updated", Toast.LENGTH_SHORT).show()
                    familyAdapter.updateData(familyList)
                } else {
                    Toast.makeText(requireContext(), "Failed to update family member", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userListener?.let { try { userRef.removeEventListener(it) } catch (_: Exception) {} }
        userListener = null
    }
}
