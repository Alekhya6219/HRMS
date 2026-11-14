import React, { useEffect, useMemo, useState } from 'react';
import { LayoutDashboard, Users, CalendarCheck, CalendarDays, Wallet, Building2, Search, Bell, Plus, Menu, X, Pencil, Trash2, Check, XCircle, Clock3, TrendingUp, UserCheck, CalendarClock, IndianRupee, ArrowUpRight } from 'lucide-react';
import api from './api';
import Login from './Login';
const money = n => new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR',maximumFractionDigits:0}).format(Number(n||0));
const today = new Date().toISOString().slice(0,10);
const initials = name => (name||'?').split(' ').map(x=>x[0]).slice(0,2).join('').toUpperCase();

export default function App(){
    const [user, setUser] = useState(() => {
  try {
    const token = sessionStorage.getItem('peoplepulse_token');
    const savedUser = sessionStorage.getItem('peoplepulse_user');

    return token && savedUser ? JSON.parse(savedUser) : null;
  } catch {
    return null;
  }
});
 const [page,setPage]=useState('Dashboard'),[mobileOpen,setMobileOpen]=useState(false);
 const [employees,setEmployees]=useState([]),[attendance,setAttendance]=useState([]),[leaves,setLeaves]=useState([]),[payroll,setPayroll]=useState([]),[departments,setDepartments]=useState([]),[stats,setStats]=useState({});
 const [search,setSearch]=useState(''),[modal,setModal]=useState(''),[editing,setEditing]=useState(null),[toast,setToast]=useState('');
 const [employeeForm,setEmployeeForm]=useState({name:'',email:'',phone:'',designation:'',department:'Engineering',joiningDate:today,salary:'',status:'ACTIVE'});
 const [attendanceForm,setAttendanceForm]=useState({employeeId:'',date:today,status:'PRESENT',checkIn:'09:00',checkOut:'18:00'});
 const [leaveForm,setLeaveForm]=useState({employeeId:'',leaveType:'Casual Leave',startDate:today,endDate:today,reason:''});
 const [payForm,setPayForm]=useState({employeeId:'',month:new Date().toISOString().slice(0,7),basicSalary:'',deductions:'0',status:'PENDING'});
 const nav =
  user?.role === 'ADMIN'
    ? [
        ['Dashboard', LayoutDashboard],
        ['Employees', Users],
        ['Attendance', CalendarCheck],
        ['Leave Management', CalendarDays],
        ['Payroll', Wallet],
        ['Departments', Building2],
      ]
    : user?.role === 'HR'
    ? [
        ['Dashboard', LayoutDashboard],
        ['Employees', Users],
        ['Attendance', CalendarCheck],
        ['Leave Management', CalendarDays],
        ['Departments', Building2],
      ]
    : [
        ['My Profile', Users],
        ['My Attendance', CalendarCheck],
        ['My Leaves', CalendarDays],
        ['My Payroll', Wallet],
      ];
 const notify=t=>{setToast(t);setTimeout(()=>setToast(''),2800)};
 const handleLogout = () => {
  sessionStorage.removeItem('peoplepulse_token');
  sessionStorage.removeItem('peoplepulse_user');
  setUser(null);
  setPage('Dashboard');
};
 const load = async () => {
  try {
    const role = user?.role;

    if (role === 'EMPLOYEE') {
      const [e, a, l, p, d] = await Promise.all([
        api.get('/employees/me'),
        api.get('/attendance/me'),
        api.get('/leaves/me'),
        api.get('/payroll/me'),
        api.get('/departments'),
      ]);

      setEmployees(e.data ? [e.data] : []);
      setAttendance(a.data);
      setLeaves(l.data);
      setPayroll(p.data);
      setDepartments(d.data);
      setStats({});
      return;
    }

    const requests = [
      api.get('/employees'),
      api.get('/attendance'),
      api.get('/leaves'),
      role === 'ADMIN' ? api.get('/payroll') : Promise.resolve({ data: [] }),
      api.get('/departments'),
      api.get('/dashboard'),
    ];

    const [e, a, l, p, d, s] = await Promise.all(requests);

    setEmployees(e.data);
    setAttendance(a.data);
    setLeaves(l.data);
    setPayroll(p.data);
    setDepartments(d.data);
    setStats(s.data);
    } catch (err) {
    notify('Unable to load dashboard data. Please sign in again if your session has expired.');
  }
};
 useEffect(() => {
  if (user) {
    load();
  }
}, [user]);
 const filtered=useMemo(()=>employees.filter(e=>(e.name+' '+e.email+' '+e.department+' '+e.designation).toLowerCase().includes(search.toLowerCase())),[employees,search]);
 const openEmployee=(e=null)=>{setEditing(e);setEmployeeForm(e?{...e,joiningDate:e.joiningDate||today}:{name:'',email:'',phone:'',designation:'',department:'Engineering',joiningDate:today,salary:'',status:'ACTIVE'});setModal('employee')};
 const saveEmployee=async ev=>{ev.preventDefault();try{if(editing)await api.put('/employees/'+editing.id,employeeForm);else await api.post('/employees',employeeForm);setModal('');notify(editing?'Employee updated':'Employee added');load()}catch(e){notify(e.response?.data?.message||'Unable to save employee. Check required fields and unique email.')}};
 const deleteEmployee=async id=>{if(!confirm('Delete this employee?'))return;try{await api.delete('/employees/'+id);notify('Employee deleted');load()}catch(e){notify('Unable to delete employee')}};
 const markAttendance=async ev=>{ev.preventDefault();try{await api.post('/attendance',{...attendanceForm,employeeId:Number(attendanceForm.employeeId),checkIn:attendanceForm.checkIn||null,checkOut:attendanceForm.checkOut||null});setModal('');notify('Attendance saved');load()}catch(e){notify('Unable to save attendance')}};
 const applyLeave=async ev=>{ev.preventDefault();try{await api.post('/leaves',{...leaveForm,employeeId:Number(leaveForm.employeeId)});setModal('');notify('Leave request submitted');load()}catch(e){notify('Unable to submit leave')}};
 const leaveStatus=async(id,status)=>{await api.put('/leaves/'+id+'/status',{status});notify('Leave '+status.toLowerCase());load()};
 const savePayroll=async ev=>{ev.preventDefault();try{await api.post('/payroll',{...payForm,employeeId:Number(payForm.employeeId),basicSalary:Number(payForm.basicSalary),deductions:Number(payForm.deductions)});setModal('');notify('Payroll record created');load()}catch(e){notify('Unable to create payroll record')}};
 const payrollStatus=async(id,status)=>{await api.put('/payroll/'+id+'/status',{status});load();notify('Payroll updated')};
 const field=(label,name,type='text',required=true)=><label className="field"><span>{label}</span><input type={type} required={required} value={employeeForm[name]??''} onChange={e=>setEmployeeForm({...employeeForm,[name]:e.target.value})}/></label>;
 const employeeName=id=>employees.find(e=>e.id===id)?.name||'Unknown employee';
 const badge=s=><span className={'badge '+String(s||'').toLowerCase()}>{s}</span>;
 const header=(title,sub,action)=><div className="page-head"><div><h1>{title}</h1><p>{sub}</p></div>{action}</div>;
 const stat=(label,value,icon,trend,accent)=> <div className="stat-card"><div className={'stat-icon '+accent}>{React.createElement(icon,{size:20})}</div><div className="stat-label">{label}</div><div className="stat-value">{value}</div><div className="stat-foot"><span className="up"><TrendingUp size={13}/>{trend}</span><span>vs. this month</span></div></div>;
 const actionBtn=(label,fn)=><button className="primary" onClick={fn}><Plus size={16}/>{label}</button>;
 const tableEmpty=(cols,msg)=><tr><td colSpan={cols} className="empty">{msg}</td></tr>;
if (!user) {
  return <Login onLogin={(loggedInUser) => setUser(loggedInUser)} />;
}

return <div className="app-shell">
  <aside className={'sidebar '+(mobileOpen?'show':'')}><div className="brand"><div className="brand-mark">P</div><div><b>peoplepulse</b><small>PEOPLE & CULTURE</small></div><button className="close-nav" onClick={()=>setMobileOpen(false)}><X/></button></div>
   <div className="workspace"><div className="workspace-avatar">AC</div><div><b>Acme Corporation</b><small>Workspace</small></div><span className="chevron">⌄</span></div>
   <div className="nav-label">WORKSPACE</div><nav>{nav.map(([n,I])=><button key={n} className={page===n?'active':''} onClick={()=>{setPage(n);setMobileOpen(false)}}><I size={18}/>{n}{n==='Leave Management'&&stats.pendingLeaves>0&&<em>{stats.pendingLeaves}</em>}</button>)}</nav>
   <div className="sidebar-bottom"><div className="help-card"><div className="help-icon">✦</div><b>Need a hand?</b><p>Visit our help center for tips and guides.</p><button onClick={()=>notify('Help center: demo workspace')}>Visit help center <ArrowUpRight size={13}/></button></div>
   <div className="profile">
  <div className="profile-avatar">
    {initials(user?.email)}
  </div>
  <div>
    <b>{user?.email}</b>
    <small>{user?.role}</small>
  </div>
  <button
    type="button"
    className="outline"
    onClick={handleLogout}
  >
    Logout
  </button>
</div>
</div>
  </aside>
  {mobileOpen&&<div className="scrim" onClick={()=>setMobileOpen(false)}/>}
  <main className="main"><header className="topbar"><button className="mobile-menu" onClick={()=>setMobileOpen(true)}><Menu/></button><div className="crumb">Workspace <span>/</span> <b>{page}</b></div><div className="top-actions"><div className="today-pill"><span className="green-dot"/>{new Date().toLocaleDateString('en-IN',{weekday:'short',day:'numeric',month:'short',year:'numeric'})}</div><button className="icon-btn" onClick={()=>notify('You are all caught up')}><Bell size={18}/><i/></button><div className="top-avatar">AD</div></div></header>
  <div className="content">
   {page==='Dashboard'&&<>{header('Good morning, Admin 👋',"Here's what's happening with your people today.",<button className="outline" onClick={()=>openEmployee()}><Plus size={16}/> Add employee</button>)}
    <div className="stats-grid">{stat('Total employees',stats.totalEmployees||0,Users,'8.2%','purple')}{stat('Present today',stats.presentToday||0,UserCheck,'Today','green')}{stat('Pending leave requests',stats.pendingLeaves||0,CalendarClock,'Needs review','orange')}{stat('Payroll processed',money(stats.payrollTotal),IndianRupee,'This period','blue')}</div>
    <div className="dashboard-grid"><section className="panel"><div className="panel-head"><div><h3>Recent employees</h3><p>Your latest team members</p></div><button className="text-btn" onClick={()=>setPage('Employees')}>View all <ArrowUpRight size={14}/></button></div><div className="table-wrap"><table><thead><tr><th>EMPLOYEE</th><th>DEPARTMENT</th><th>JOINED DATE</th><th>STATUS</th></tr></thead><tbody>{employees.slice(0,5).map(e=><tr key={e.id}><td><div className="person"><div className="person-avatar">{initials(e.name)}</div><div><b>{e.name}</b><small>{e.designation}</small></div></div></td><td>{e.department}</td><td>{e.joiningDate}</td><td>{badge(e.status)}</td></tr>)||tableEmpty(4,'No employees yet')}</tbody></table></div></section>
     <section className="panel"><div className="panel-head"><div><h3>Leave requests</h3><p>Requests waiting for review</p></div><button className="text-btn" onClick={()=>setPage('Leave Management')}>View all <ArrowUpRight size={14}/></button></div><div className="leave-list">{leaves.filter(l=>l.status==='PENDING').slice(0,4).map(l=><div className="leave-item" key={l.id}><div className="person-avatar">{initials(l.employeeName)}</div><div className="leave-info"><b>{l.employeeName}</b><small>{l.leaveType} · {l.startDate}</small></div><span className="pending-dot">Pending</span></div>)}{leaves.filter(l=>l.status==='PENDING').length===0&&<div className="empty">No pending leave requests 🎉</div>}</div></section></div>
    <section className="panel quick-panel"><div><h3>Quick actions</h3><p>Common tasks, one click away</p></div><div className="quick-actions"><button onClick={()=>openEmployee()}><span className="qa purple">＋</span><b>Add employee</b><small>Create a new employee profile</small></button><button onClick={()=>{setAttendanceForm({employeeId:employees[0]?.id||'',date:today,status:'PRESENT',checkIn:'09:00',checkOut:'18:00'});setModal('attendance')}}><span className="qa green">◷</span><b>Mark attendance</b><small>Record today's attendance</small></button><button onClick={()=>{setLeaveForm({...leaveForm,employeeId:employees[0]?.id||''});setModal('leave')}}><span className="qa orange">▤</span><b>Apply for leave</b><small>Submit a leave request</small></button></div></section>
   </>}
   {page==='Employees'&&<>{header('Employees','Manage your people, all in one place.',actionBtn('Add employee',()=>openEmployee()))}<div className="panel"><div className="toolbar"><div className="search"><Search size={17}/><input placeholder="Search by name, role or department..." value={search} onChange={e=>setSearch(e.target.value)}/></div><span className="muted">{filtered.length} employees</span></div><div className="table-wrap"><table><thead><tr><th>EMPLOYEE</th><th>DEPARTMENT</th><th>EMAIL</th><th>JOINED</th>{user?.role === 'ADMIN' && <th>SALARY</th>}<th>STATUS</th><th></th></tr></thead><tbody>{filtered.map(e=><tr key={e.id}><td><div className="person"><div className="person-avatar">{initials(e.name)}</div><div><b>{e.name}</b><small>{e.designation}</small></div></div></td><td>{e.department}</td><td>{e.email}</td><td>{e.joiningDate}</td>{user?.role === 'ADMIN' && <td>{money(e.salary)}</td>}<td>{badge(e.status)}</td><td><div className="row-actions"><button title="Edit" onClick={()=>openEmployee(e)}><Pencil size={15}/></button><button title="Delete" onClick={()=>deleteEmployee(e.id)}><Trash2 size={15}/></button></div></td></tr>)}{filtered.length===0&&tableEmpty(user?.role === 'ADMIN' ? 7 : 6,'No employees found. Add your first employee.')}</tbody></table></div></div></>}
   {page==='Attendance'&&<>{header('Attendance','Track daily attendance and working hours.',actionBtn('Mark attendance',()=>{setAttendanceForm({employeeId:employees[0]?.id||'',date:today,status:'PRESENT',checkIn:'09:00',checkOut:'18:00'});setModal('attendance')}))}<div className="stats-grid small-stats">{stat('Present today',attendance.filter(a=>a.status==='PRESENT').length,UserCheck,'Today','green')}{stat('Late arrivals',attendance.filter(a=>a.status==='LATE').length,Clock3,'Today','orange')}{stat('Absent',attendance.filter(a=>a.status==='ABSENT').length,Users,'Today','purple')}</div><section className="panel"><div className="panel-head"><div><h3>Attendance records</h3><p>Records for {today}</p></div><span className="badge present">{attendance.length} records</span></div><div className="table-wrap"><table><thead><tr><th>EMPLOYEE</th><th>DATE</th><th>CHECK IN</th><th>CHECK OUT</th><th>STATUS</th></tr></thead><tbody>{attendance.map(a=><tr key={a.id}><td><b>{employeeName(a.employeeId)}</b></td><td>{a.date}</td><td>{a.checkIn||'—'}</td><td>{a.checkOut||'—'}</td><td>{badge(a.status)}</td></tr>)}{attendance.length===0&&tableEmpty(5,'No attendance marked today.')}</tbody></table></div></section></>}
   {page==='Leave Management'&&<>{header('Leave management','Review requests and keep leave balances organized.',actionBtn('New leave request',()=>{setLeaveForm({...leaveForm,employeeId:employees[0]?.id||''});setModal('leave')}))}<section className="panel"><div className="panel-head"><div><h3>All leave requests</h3><p>Approve or reject employee leave applications</p></div><span className="badge pending">{leaves.filter(l=>l.status==='PENDING').length} pending</span></div><div className="table-wrap"><table><thead><tr><th>EMPLOYEE</th><th>LEAVE TYPE</th><th>FROM</th><th>TO</th><th>REASON</th><th>STATUS</th><th>ACTION</th></tr></thead><tbody>{leaves.map(l=><tr key={l.id}><td><b>{l.employeeName}</b></td><td>{l.leaveType}</td><td>{l.startDate}</td><td>{l.endDate}</td><td>{l.reason}</td><td>{badge(l.status)}</td><td>{l.status==='PENDING'?<div className="row-actions"><button className="approve" onClick={()=>leaveStatus(l.id,'APPROVED')} title="Approve"><Check size={16}/></button><button className="reject" onClick={()=>leaveStatus(l.id,'REJECTED')} title="Reject"><XCircle size={16}/></button></div>:'—'}</td></tr>)}{leaves.length===0&&tableEmpty(7,'No leave requests yet.')}</tbody></table></div></section></>}
   {page==='Payroll'&&<>{header('Payroll','Manage salary records, deductions and payments.',actionBtn('Create payroll',()=>{setPayForm({employeeId:employees[0]?.id||'',month:new Date().toISOString().slice(0,7),basicSalary:'',deductions:'0',status:'PENDING'});setModal('payroll')}))}<div className="stats-grid small-stats">{stat('Total payroll',money(stats.payrollTotal),IndianRupee,'All records','blue')}{stat('Paid records',payroll.filter(p=>p.status==='PAID').length,Check,'Completed','green')}{stat('Pending records',payroll.filter(p=>p.status==='PENDING').length,Clock3,'Awaiting payment','orange')}</div><section className="panel"><div className="panel-head"><div><h3>Payroll records</h3><p>Salary details by employee and month</p></div></div><div className="table-wrap"><table><thead><tr><th>EMPLOYEE</th><th>MONTH</th><th>BASIC SALARY</th><th>DEDUCTIONS</th><th>NET SALARY</th><th>STATUS</th><th>ACTION</th></tr></thead><tbody>{payroll.map(p=><tr key={p.id}><td><b>{p.employeeName}</b></td><td>{p.month}</td><td>{money(p.basicSalary)}</td><td>{money(p.deductions)}</td><td><b>{money(p.netSalary)}</b></td><td>{badge(p.status)}</td><td>{p.status==='PENDING'?<button className="small-primary" onClick={()=>payrollStatus(p.id,'PAID')}>Mark paid</button>:'—'}</td></tr>)}{payroll.length===0&&tableEmpty(7,'No payroll records. Create the first payroll entry.')}</tbody></table></div></section></>}
   {page==='Departments'&&<>{header('Departments','Your organization structure at a glance.')}<div className="department-grid">{departments.map((d,i)=><div className="department-card" key={d.id}><div className={'dept-icon d'+i}><Building2 size={20}/></div><h3>{d.name}</h3><p>{employees.filter(e=>e.department===d.name).length} team members</p><div className="dept-people">{employees.filter(e=>e.department===d.name).slice(0,4).map(e=><span title={e.name} key={e.id}>{initials(e.name)}</span>)}</div></div>)}</div></>}
  </div><footer>© 2026 PeoplePulse · Made for better workplaces <span>v1.0.0</span></footer></main>
  {modal&&<div className="modal-backdrop" onMouseDown={e=>e.target===e.currentTarget&&setModal('')}><div className="modal"><div className="modal-head"><div><h2>{modal==='employee'?(editing?'Edit employee':'Add employee'):modal==='attendance'?'Mark attendance':modal==='leave'?'New leave request':'Create payroll'}</h2><p>Enter the details below to continue.</p></div><button onClick={()=>setModal('')}><X/></button></div>
   {modal==='employee'&&<form onSubmit={saveEmployee}><div className="form-grid">{field('Full name','name')}{field('Email address','email','email')}{field('Phone number','phone')}{field('Designation','designation')}<label className="field"><span>Department</span><select value={employeeForm.department} onChange={e=>setEmployeeForm({...employeeForm,department:e.target.value})}>{departments.map(d=><option key={d.id}>{d.name}</option>)}</select></label>{field('Joining date','joiningDate','date')}{field('Monthly salary (₹)','salary','number')}<label className="field"><span>Status</span><select value={employeeForm.status} onChange={e=>setEmployeeForm({...employeeForm,status:e.target.value})}><option>ACTIVE</option><option>INACTIVE</option></select></label></div><div className="modal-actions"><button type="button" className="outline" onClick={()=>setModal('')}>Cancel</button><button className="primary">Save employee</button></div></form>}
   {modal==='attendance'&&<form onSubmit={markAttendance}><div className="form-grid"><label className="field full"><span>Employee</span><select required value={attendanceForm.employeeId} onChange={e=>setAttendanceForm({...attendanceForm,employeeId:e.target.value})}>{employees.map(e=><option value={e.id} key={e.id}>{e.name}</option>)}</select></label><label className="field"><span>Date</span><input type="date" value={attendanceForm.date} onChange={e=>setAttendanceForm({...attendanceForm,date:e.target.value})}/></label><label className="field"><span>Status</span><select value={attendanceForm.status} onChange={e=>setAttendanceForm({...attendanceForm,status:e.target.value})}><option>PRESENT</option><option>LATE</option><option>ABSENT</option><option>WORK_FROM_HOME</option></select></label><label className="field"><span>Check-in</span><input type="time" value={attendanceForm.checkIn} onChange={e=>setAttendanceForm({...attendanceForm,checkIn:e.target.value})}/></label><label className="field"><span>Check-out</span><input type="time" value={attendanceForm.checkOut} onChange={e=>setAttendanceForm({...attendanceForm,checkOut:e.target.value})}/></label></div><div className="modal-actions"><button type="button" className="outline" onClick={()=>setModal('')}>Cancel</button><button className="primary">Save attendance</button></div></form>}
   {modal==='leave'&&<form onSubmit={applyLeave}><div className="form-grid"><label className="field full"><span>Employee</span><select required value={leaveForm.employeeId} onChange={e=>setLeaveForm({...leaveForm,employeeId:e.target.value})}>{employees.map(e=><option value={e.id} key={e.id}>{e.name}</option>)}</select></label><label className="field"><span>Leave type</span><select value={leaveForm.leaveType} onChange={e=>setLeaveForm({...leaveForm,leaveType:e.target.value})}><option>Casual Leave</option><option>Sick Leave</option><option>Earned Leave</option><option>Unpaid Leave</option></select></label><label className="field"><span>Start date</span><input type="date" required value={leaveForm.startDate} onChange={e=>setLeaveForm({...leaveForm,startDate:e.target.value})}/></label><label className="field"><span>End date</span><input type="date" required value={leaveForm.endDate} onChange={e=>setLeaveForm({...leaveForm,endDate:e.target.value})}/></label><label className="field full"><span>Reason</span><textarea required rows="3" value={leaveForm.reason} onChange={e=>setLeaveForm({...leaveForm,reason:e.target.value})}/></label></div><div className="modal-actions"><button type="button" className="outline" onClick={()=>setModal('')}>Cancel</button><button className="primary">Submit request</button></div></form>}
   {modal==='payroll'&&<form onSubmit={savePayroll}><div className="form-grid"><label className="field full"><span>Employee</span><select required value={payForm.employeeId} onChange={e=>setPayForm({...payForm,employeeId:e.target.value})}>{employees.map(e=><option value={e.id} key={e.id}>{e.name}</option>)}</select></label><label className="field"><span>Payroll month</span><input type="month" required value={payForm.month} onChange={e=>setPayForm({...payForm,month:e.target.value})}/></label><label className="field"><span>Basic salary (₹)</span><input type="number" min="0" required value={payForm.basicSalary} onChange={e=>setPayForm({...payForm,basicSalary:e.target.value})}/></label><label className="field"><span>Deductions (₹)</span><input type="number" min="0" value={payForm.deductions} onChange={e=>setPayForm({...payForm,deductions:e.target.value})}/></label></div><div className="modal-actions"><button type="button" className="outline" onClick={()=>setModal('')}>Cancel</button><button className="primary">Create record</button></div></form>}
  </div></div>}
  {toast&&<div className="toast">{toast}</div>}
 </div>
}